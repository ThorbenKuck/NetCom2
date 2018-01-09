package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException;
import com.github.thorbenkuck.netcom2.interfaces.RemoteObjectRegistration;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class RemoteObjectRegistrationImpl implements RemoteObjectRegistration {

	private final Map<Class<?>, Object> mapping = new HashMap<>();
	private final Logging logging = Logging.unified();

	RemoteObjectRegistrationImpl() {
		logging.debug("RemoteObjectRegistration established!");
	}

	@Override
	public void register(final Object object) {
		register(object, object.getClass());
	}

	@Override
	public void register(final Object o, final Class<?>... identifier) {
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

	@Override
	public <T> void unregister(final T t) {
		synchronized (mapping) {
			mapping.remove(t.getClass());
		}
	}

	@Override
	public void run(final RemoteAccessCommunicationModelRequest request, final Connection connection) throws RemoteRequestException {
		Class clazz = request.getClazz();
		Object o;
		synchronized (mapping) {
			o = mapping.get(clazz);
		}
		if(o == null) {
			RemoteAccessCommunicationModelResponse response = new RemoteAccessCommunicationModelResponse(request.getUuid(), new RemoteRequestException(request.getClazz() + " is not registered!"), null);
			connection.write(response);
			System.out.println(mapping.keySet());
			throw new RemoteRequestException("No registered Object found for " + request.getClazz());
		}

		Throwable throwableThrown = null;
		Object result = null;

		for(Method method : o.getClass().getMethods()) {
			if(method.getName().equals(request.getMethodName())) {
				if(method.getParameterCount() > 0) {
					logging.error("Cannot call " + method.getName() + " of " + o.getClass() + "! Parameters are not yet supported!");
				}
				if(!method.isAccessible()) {
					method.setAccessible(true);
				}
				try {
					result = method.invoke(o);
				} catch (IllegalAccessException | InvocationTargetException e) {
					throwableThrown = e;
				}
			}
		}

		RemoteAccessCommunicationModelResponse response = new RemoteAccessCommunicationModelResponse(request.getUuid(), throwableThrown, result);
		connection.write(response);

		System.gc();
	}
}
