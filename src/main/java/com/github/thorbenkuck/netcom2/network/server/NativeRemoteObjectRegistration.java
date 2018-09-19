package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.rmi.IgnoreRemoteExceptions;
import com.github.thorbenkuck.netcom2.annotations.rmi.RegistrationOverrideProhibited;
import com.github.thorbenkuck.netcom2.exceptions.RemoteObjectInvalidMethodException;
import com.github.thorbenkuck.netcom2.exceptions.RemoteObjectNotRegisteredException;
import com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.comm.RemoteAccessCommunicationRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.RemoteAccessCommunicationResponse;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.*;

class NativeRemoteObjectRegistration implements RemoteObjectRegistration {

	private static final Map<Class<?>, Class<?>> PRIMITIVE_MAPPING;

	static {
		Map<Class<?>, Class<?>> primitives = new HashMap<>();
		primitives.put(int.class, Integer.class);
		primitives.put(double.class, Double.class);
		primitives.put(long.class, Long.class);
		primitives.put(short.class, Short.class);
		primitives.put(float.class, Float.class);
		primitives.put(char.class, Character.class);
		primitives.put(boolean.class, Boolean.class);
		primitives.put(byte.class, Byte.class);

		PRIMITIVE_MAPPING = Collections.unmodifiableMap(primitives);
	}

	private final Map<Class<?>, Object> mapping = new HashMap<>();
	private final Logging logging = Logging.unified();
	private final RemoteRequestHandler remoteRequestHandler = new RemoteRequestHandler();

	@APILevel
	NativeRemoteObjectRegistration() {
		logging.instantiated(this);
	}

	private CommunicationRegistration communicationRegistration;

	/**
	 * Orders the provided arguments, to align to the method-signature
	 *
	 * @param args   the array of passed arguemts
	 * @param method the method, that should be invoked
	 * @return an correctly ordered Array.
	 */
	private Object[] orderParameters(Object[] args, Method method) {
		if (args == null) {
			return null;
		}
		List<Object> arguments = new ArrayList<>(Arrays.asList(args));
		List<Object> parameters = new ArrayList<>();

		for (Class parameterClass : method.getParameterTypes()) {
			Class casedParameter = convertPrimitiveTypes(parameterClass);
			Object o = get(arguments, casedParameter);
			parameters.add(o);
		}

		return parameters.toArray();
	}

	/**
	 * Returns a certain Object from a List of Object, based on the class provided.
	 *
	 * @param list  the List of Objects to search
	 * @param clazz the Class type which should be found.
	 * @return the Object.
	 * @throws IllegalArgumentException if the Object could not be found.
	 */
	private Object get(List<Object> list, Class clazz) {
		for (Object object : list) {
			if (convertPrimitiveTypes(object.getClass()).equals(clazz)) {
				return object;
			}
		}
		throw new IllegalArgumentException("Could not correctly determine the Objects! Possible internal error! Requested: " + clazz + " provided " + list);
	}

	/**
	 * Describes, whether or not, the provided class type may be overridden within the internal mapping.
	 * <p>
	 * It utilizes the {@link RegistrationOverrideProhibited} annotation to check, whether or not, the currently saved
	 * instance may be overridden or not.
	 *
	 * @param clazz the class that should be checked
	 * @return true, if no annotation is present or nothing is currently saved, else false.
	 */
	private boolean canBeOverridden(Class clazz) {
		if (clazz.getAnnotation(RegistrationOverrideProhibited.class) != null) {
			logging.trace("Found RegistrationOverrideProhibited Annotation, checking if instance is saved");
			Object check;
			synchronized (mapping) {
				check = mapping.get(clazz);
			}
			return check == null;
		}
		return true;
	}

	/**
	 * Generates the {@link RemoteAccessCommunicationResponse}, by the provided <code>result</code> and <code>exception</code>.
	 * <p>
	 * If the method had no return value, pass null as the result.
	 *
	 * @param uuid      the UUID of the RemoteObject
	 * @param exception the  encountered Exception (may be null)
	 * @param result    the result of the Method-call (may be null)
	 * @param clazz     the RemoteObjectClass
	 * @param method    the method, that was invoked
	 * @return am encapsulated Result-Object
	 */
	private RemoteAccessCommunicationResponse generateResult(UUID uuid, Exception exception, Object result, Class clazz, Method method) {
		if (ignoreThrowable(exception, clazz, method)) {
			return new RemoteAccessCommunicationResponse(uuid, null, result);
		}
		return new RemoteAccessCommunicationResponse(uuid, exception, result);

	}

	/**
	 * Checks whether or not the provided Exception should be thrown.
	 * <p>
	 * For this Check, this method does rely on the {@link IgnoreRemoteExceptions} annotation.
	 * <p>
	 * By default, this method will return false, which means any Exception will be thrown. Only if the {@link IgnoreRemoteExceptions}
	 * annotation is present and does not contain the provided Exception within {@link IgnoreRemoteExceptions#exceptTypes()},
	 * this method will return false.
	 *
	 * @param exception         the Exception, that should be checked
	 * @param annotatedElements all annotated elements
	 * @return false, if the Exception should be thrown, true if not.
	 */
	private boolean ignoreThrowable(Exception exception, AnnotatedElement... annotatedElements) {
		if (exception != null) {
			for (AnnotatedElement element : annotatedElements) {
				if (element == null) {
					continue;
				}
				IgnoreRemoteExceptions annotation = element.getAnnotation(IgnoreRemoteExceptions.class);
				if (annotation != null) {
					return !Arrays.asList(annotation.exceptTypes()).contains(exception.getClass());
				}
			}
		}
		return false;
	}

	/**
	 * This method converts primitive types to their wrapper types.
	 * <p>
	 * Because of the way, the java.io serialization runs, primitive classes are changed to their wrapper types.
	 * This mean, that remote-methods, that declare a primitive type, will never be called, because the arguments do not
	 * match.
	 * <p>
	 * This method was introduced because of the Issue#50
	 *
	 * @param input the potential primitive type
	 * @return the Wrapper type, or the type that provided if not primitive.
	 */
	private Class<?> convertPrimitiveTypes(Class<?> input) {
		return PRIMITIVE_MAPPING.getOrDefault(input, input);
	}

	/**
	 * Checks, whether or not the required arguments and the provided arguments do match.
	 *
	 * @param method the Method which should be called
	 * @param args   the arguments passed over
	 * @return true, if all arguments are of the right type in the right order.
	 */
	private boolean parameterTypesEqual(Method method, Object[] args) {
		Class<?>[] declaredParameterTypes = method.getParameterTypes();
		if (args == null) {
			return declaredParameterTypes.length == 0;
		}
		if (args.length != declaredParameterTypes.length) {
			return false;
		}
		for (int i = 0; i < args.length; i++) {
			Class<?> declaredType = convertPrimitiveTypes(declaredParameterTypes[i]);
			Class<?> argumentType = convertPrimitiveTypes(args[i].getClass());
			// This check, checks for Session
			// or Connection types as well as for
			// the declared type.  This means,
			// if you would be able to, you could inject
			// a Session or Connection into an Method-Declaration
			// and still be running this RMI API.
			// This is not relevant for Java.
			if (!declaredType.equals(argumentType)
					|| declaredParameterTypes[i].equals(Session.class)
					|| declaredParameterTypes[i].equals(Connection.class)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Removes the provided Class from the internal mapping
	 *
	 * @param clazz the Class, that should be unregistered.
	 */
	private void unregisterCertainClass(Class clazz) {
		logging.trace("Unregister " + clazz);
		synchronized (mapping) {
			mapping.remove(clazz);
		}
	}

	/**
	 * Calls the Method, that we.printStackTrace();as given in an safe environment.
	 * <p>
	 * It returns the computed Object, of the method-call. May throw an Throwable, if the Object <code>callOn</code> throws
	 * an throwable while executing the Method.
	 * <p>
	 * It does not check, whether or not the parameters are in the right order or of the right type.
	 *
	 * @param method the Method that should be called
	 * @param callOn the object that method should be called upon
	 * @param args   the arguments, that are passed to the method-call
	 * @return the computed Result of the Object
	 * @throws Throwable any throwable the Object throws
	 */
	private Object handleMethod(Method method, Object callOn, Object[] args) throws Throwable {
		final boolean accessible = method.isAccessible();
		logging.trace("updating accessibility of Method " + method.getName());
		method.setAccessible(true);

		try {
			logging.trace("invoking Method " + method.getName() + " of " + callOn + " with parameters " + Arrays.toString(args));
			return method.invoke(callOn, args);
		} finally {
			logging.trace("Setting accessibility back to original state(" + accessible + ")..");
			method.setAccessible(accessible);
		}
	}

	@Override
	public void setup(ServerStart serverStart) {
		communicationRegistration = serverStart.getCommunicationRegistration();

		try {
			communicationRegistration.acquire();
			communicationRegistration.register(RemoteAccessCommunicationRequest.class)
					.addFirst(remoteRequestHandler);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			communicationRegistration.release();
		}
	}

	@Override
	public void close() {
		try {
			communicationRegistration.acquire();
			communicationRegistration.register(RemoteAccessCommunicationRequest.class)
					.remove(remoteRequestHandler);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			communicationRegistration.release();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(final Object object) {
		NetCom2Utils.parameterNotNull(object);
		register(object, object.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(final Object o, final Class<?>... identifier) {
		NetCom2Utils.parameterNotNull(o, identifier);
		if (identifier.length <= 0) {
			throw new IllegalArgumentException("At least on identifier class is required to register an Object!");
		}
		logging.debug("Trying to register " + o.getClass() + " by " + Arrays.asList(identifier));
		for (Class<?> clazz : identifier) {
			logging.debug("Assignable " + clazz.isAssignableFrom(o.getClass()));
			if (!clazz.isAssignableFrom(o.getClass())) {
				logging.error("The Object " + o.getClass() + " is not assignable from " + clazz);
				continue;
			}

			Object savedInstance;
			synchronized (mapping) {
				savedInstance = mapping.get(clazz);
			}
			if (savedInstance != null && !canBeOverridden(savedInstance.getClass())) {
				logging.debug("Overriding of " + clazz + " not possible due to its annotation at " + savedInstance);
				continue;
			}

			logging.trace("Registering " + clazz + " as RemoteUsable by Object " + o.getClass());
			synchronized (mapping) {
				mapping.put(clazz, o);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void hook(Object object) {
		NetCom2Utils.parameterNotNull(object);
		List<Class<?>> classList = new ArrayList<>(Arrays.asList(object.getClass().getInterfaces()));
		classList.add(object.getClass().getSuperclass());
		classList.add(object.getClass());
		register(object, classList.toArray(new Class[classList.size()]));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregister(final Object object) {
		NetCom2Utils.parameterNotNull(object);
		unregister(object, object.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregister(Object object, Class... identifiers) {
		NetCom2Utils.parameterNotNull(object, identifiers);
		logging.debug("Trying to unregister " + object.getClass() + ", identified by " + Arrays.asList(identifiers));
		for (Class<?> clazz : identifiers) {
			logging.debug("Assignable " + clazz.isAssignableFrom(object.getClass()));
			Object selected;
			synchronized (mapping) {
				selected = mapping.get(clazz);
			}

			if (selected == null) {
				logging.warn("No instance registered for " + clazz + ".. Tried to unregister " + object);
				continue;
			}
			if (!object.equals(selected)) {
				logging.error("The Object " + object.getClass() + " is not assignable from " + clazz);
				continue;
			}
			unregisterCertainClass(clazz);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregister(Class... identifier) {
		NetCom2Utils.parameterNotNull(identifier);
		for (Class clazz : identifier) {
			unregisterCertainClass(clazz);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unhook(Object object) {
		NetCom2Utils.parameterNotNull(object);
		List<Class> classList = new ArrayList<>(Arrays.asList(object.getClass().getInterfaces()));
		classList.add(object.getClass().getSuperclass());
		classList.add(object.getClass());
		unregister(object, classList.toArray(new Class[classList.size()]));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		logging.debug("Clearing the RemoteObjectRegistration " + toString());
		synchronized (mapping) {
			mapping.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RemoteAccessCommunicationResponse run(final RemoteAccessCommunicationRequest request) {
		NetCom2Utils.parameterNotNull(request);
		NetCom2Utils.parameterNotNull(request.getMethodName(), request.getClazz(), request.getUuid());
		final Object handlingObject;
		synchronized (mapping) {
			handlingObject = mapping.get(request.getClazz());
		}
		if (handlingObject == null) {
			logging.error("No registered Objects found for " + request.getClazz());
			logging.trace("Returning exception for no registered Object..");
			return generateResult(request.getUuid(), new RemoteObjectNotRegisteredException(request.getClazz() + " is not registered!"), null, request.getClazz(), null);
		}

		Exception exception = null;
		Object methodCallResult = null;
		Method methodToCall = null;

		for (Method method : handlingObject.getClass().getMethods()) {
			if (method.getName().equals(request.getMethodName()) && parameterTypesEqual(method, request.getParameters())) {
				logging.debug("Found suitable Method " + method.getName() + " of " + handlingObject);
				methodToCall = method;
				break;
			}
		}

		if (methodToCall != null) {
			Object[] args = orderParameters(request.getParameters(), methodToCall);
			try {
				methodCallResult = handleMethod(methodToCall, handlingObject, args);
				logging.debug("Computed result detected: " + methodCallResult);
			} catch (final Exception e) {
				logging.catching(e);
				exception = e;
			} catch (final Throwable throwable) {
				logging.fatal("Encountered throwable, non Exception: " + throwable + " while executing " + methodToCall + " on " + handlingObject.getClass(), throwable);
				exception = new RemoteException("RemoteObjectRegistration encountered " + throwable.getClass());
			}
		} else {
			exception = new RemoteObjectInvalidMethodException("No suitable method found for name " + request.getMethodName() + " with parameters" + Arrays.toString(request.getParameters()));
		}
		logging.trace("Finalizing run of " + request.getClazz());

		return generateResult(request.getUuid(), exception, methodCallResult, request.getClazz(), methodToCall);
	}

	private final class RemoteRequestHandler implements OnReceiveTriple<RemoteAccessCommunicationRequest> {

		@Override
		public void accept(ConnectionContext connectionContext, Session session, RemoteAccessCommunicationRequest remoteAccessCommunicationRequest) {
			NetCom2Utils.parameterNotNull(connectionContext, remoteAccessCommunicationRequest);
			try {
				connectionContext.send(run(remoteAccessCommunicationRequest));
			} catch (RemoteRequestException e) {
				logging.error("Could not run RemoteObjectRequest", e);
			}
		}
	}
}
