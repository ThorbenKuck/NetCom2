package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.remoteObjects.IgnoreRemoteExceptions;
import com.github.thorbenkuck.netcom2.annotations.remoteObjects.RegistrationOverrideProhibited;
import com.github.thorbenkuck.netcom2.exceptions.RemoteObjectNotRegisteredException;
import com.github.thorbenkuck.netcom2.interfaces.RemoteObjectRegistration;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationResponse;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.*;

@APILevel
class RemoteObjectRegistrationImpl implements RemoteObjectRegistration {

	private final Map<Class<?>, Object> mapping = new HashMap<>();
	private final Logging logging = Logging.unified();

	@APILevel
	RemoteObjectRegistrationImpl() {
		logging.debug("RemoteObjectRegistration established!");
	}

	private boolean canBeOverridden(Class clazz) {
		if (clazz.getAnnotation(RegistrationOverrideProhibited.class) != null) {
			logging.trace("Found RegistrationOverrideProhibited Annotation, checking if instance is saved");
			Object check;
			synchronized (mapping) {
				check = mapping.get(clazz);
			}
			if (check != null) {
				return false;
			}
		}
		return true;
	}

	private Object[] orderParameters(Object[] args, Method method) {
		if (args == null) {
			return null;
		}
		List<Object> arguments = new ArrayList<>(Arrays.asList(args));
		List<Object> parameters = new ArrayList<>();

		for (Class parameterClass : method.getParameterTypes()) {
			Object o = get(arguments, parameterClass);
			parameters.add(o);
		}

		return parameters.toArray();
	}

	private Object get(List<Object> array, Class clazz) {
		for (Object object : array) {
			if (object.getClass().equals(clazz)) {
				return object;
			}
		}
		throw new IllegalArgumentException("Could not correctly determine the Objects! Possible internal error! Requested: " + clazz + " provided " + array);
	}

	/**
	 * Calls the Method, that was given in an safe environment.
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

	/**
	 * Generates the {@link RemoteAccessCommunicationResponse}, by the provided <code>result</code> and <code>exception</code>
	 *
	 * @param uuid
	 * @param exception
	 * @param result
	 * @param clazz
	 * @param method
	 * @return
	 */
	private RemoteAccessCommunicationResponse generateResult(UUID uuid, Exception exception, Object result, Class clazz, Method method) {
		if (ignoreThrowable(exception, clazz, method)) {
			return new RemoteAccessCommunicationResponse(uuid, null, result);
		}
		return new RemoteAccessCommunicationResponse(uuid, exception, result);

	}

	private boolean ignoreThrowable(Exception exception, AnnotatedElement... annotatedElements) {
		if (exception != null) {
			for (AnnotatedElement element : annotatedElements) {
				if (element == null) {
					continue;
				}
				IgnoreRemoteExceptions annotation = element.getAnnotation(IgnoreRemoteExceptions.class);
				if (annotation != null) {
					return ! Arrays.asList(annotation.exceptTypes()).contains(exception.getClass());
				}
			}
		}
		return false;
	}

	private boolean parameterTypesEqual(Method method, Object[] args) {
		Class<?>[] declaredParameterTypes = method.getParameterTypes();
		if (args == null) {
			return declaredParameterTypes.length == 0;
		}
		if (args.length != declaredParameterTypes.length) {
			return false;
		}
		for (int i = 0; i < args.length; i++) {
			if (! declaredParameterTypes[i].equals(args[i].getClass())
					|| declaredParameterTypes[i].equals(Session.class)
					|| declaredParameterTypes[i].equals(Connection.class)) {
				return false;
			}
		}
		return true;
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
			if (! clazz.isAssignableFrom(o.getClass())) {
				logging.error("The Object " + o.getClass() + " is not assignable from " + clazz);
				continue;
			}

			Object savedInstance;
			synchronized (mapping) {
				savedInstance = mapping.get(clazz);
			}
			if (savedInstance != null && ! canBeOverridden(savedInstance.getClass())) {
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
			if (! object.equals(selected)) {
				logging.error("The Object " + object.getClass() + " is not assignable from " + clazz);
				continue;
			}
			unregister(clazz);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregister(Class... identifier) {
		for (Class clazz : identifier) {
			logging.trace("Unregister " + clazz);
			synchronized (mapping) {
				mapping.remove(clazz);
			}
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
		Method calledMethod = null;

		logging.trace("Checking declared methods of object " + handlingObject);
		for (Method method : handlingObject.getClass().getMethods()) {
			if (method.getName().equals(request.getMethodName()) && parameterTypesEqual(method, request.getParameters())) {
				Object[] args = orderParameters(request.getParameters(), method);
				logging.debug("Found suitable Method " + method.getName() + " of " + handlingObject);
				try {
					methodCallResult = handleMethod(method, handlingObject, args);
					logging.debug("Computed result detected: " + methodCallResult);
					break;
				} catch (final Exception e) {
					logging.catching(e);
					exception = e;
					break;
				} catch (final Throwable throwable) {
					logging.fatal("Encountered throwable, non Exception: " + throwable + " while executing " + method + " on " + handlingObject.getClass(), throwable);
					exception = new RemoteException("RemoteObjectRegistration encountered " + throwable.getClass());
				} finally {
					calledMethod = method;
				}
			}
		}
		logging.trace("Finalizing run of " + request.getClazz());
		return generateResult(request.getUuid(), exception, methodCallResult, request.getClazz(), calledMethod);
	}
}
