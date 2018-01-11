package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException;
import com.github.thorbenkuck.netcom2.interfaces.RemoteObjectRegistration;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelResponse;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.lang.reflect.Method;
import java.util.*;

@APILevel
class RemoteObjectRegistrationImpl implements RemoteObjectRegistration {

	private final Map<Class<?>, Object> mapping = new HashMap<>();
	private final Logging logging = Logging.unified();

	@APILevel
	RemoteObjectRegistrationImpl() {
		logging.debug("RemoteObjectRegistration established!");
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
		if(identifier.length <= 0) {
			throw new IllegalArgumentException("At least on identifier class is required to register an Object!");
		}
		logging.debug("Trying to register " + o.getClass() + " by " + Arrays.asList(identifier));
		for(Class<?> clazz : identifier) {
			logging.debug("Assignable " + clazz.isAssignableFrom(o.getClass()));
			if (!clazz.isAssignableFrom(o.getClass())) {
				logging.error("The Object " + o.getClass() + " is not assignable from " + clazz);
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
		List<Class> classList = new ArrayList<>();
		classList.addAll(Arrays.asList(object.getClass().getInterfaces()));
		classList.add(object.getClass());
		register(object, classList.toArray(new Class[classList.size()]));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregister(final Object object) {
		synchronized (mapping) {
			mapping.remove(object.getClass());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object run(final RemoteAccessCommunicationModelRequest request) throws RemoteRequestException {
		final Object handlingObject;
		synchronized (mapping) {
			handlingObject = mapping.get(request.getClazz());
		}
		if(handlingObject == null) {
			logging.error("No registered Objects found for " + request.getClazz());
			return generateResult(request.getUuid(), new RemoteRequestException(request.getClazz() + " is not registered!"), null);
		}

		Throwable throwableThrown = null;
		Object result = null;

		for(Method method : handlingObject.getClass().getMethods()) {
			if(method.getName().equals(request.getMethodName()) && parameterTypesEqual(method, request.getParameters())) {
				try {
					result = handleMethod(method, handlingObject, request.getParameters());
				} catch (final Throwable throwable) {
					throwableThrown = throwable;
				}
			}
		}
		return generateResult(request.getUuid(), throwableThrown, result);
	}

	private Object handleMethod(Method method, Object callOn, Object[] args) throws Throwable {
		final boolean accessible = method.isAccessible();
		method.setAccessible(true);

		try {
			return method.invoke(callOn, args);
		} finally {
			method.setAccessible(accessible);
		}
	}

	private Object generateResult(UUID uuid, Throwable throwable, Object result) {
		return new RemoteAccessCommunicationModelResponse(uuid, throwable, result);

	}

	private boolean parameterTypesEqual(Method method, Object[] args) {
		Class<?>[] declaredParameterTypes = method.getParameterTypes();
		if(args == null) {
			return declaredParameterTypes.length == 0;
		}
		if(args.length != declaredParameterTypes.length) {
			return false;
		}
		for(int i = 0 ; i < args.length ; i++) {
			if(!declaredParameterTypes[i].equals(args[i].getClass())) {
				return false;
			}
		}
		return true;
	}
}
