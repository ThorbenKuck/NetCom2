package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.interfaces.RemoteObjectRegistration;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RemoteObjectRegistrationImpl implements RemoteObjectRegistration {

	private final Map<Class<?>, Object> mapping = new HashMap<>();
	private final Logging logging = Logging.unified();

	@Override
	public <T> void register(final T t) {
		register(t, t.getClass());
	}

	@Override
	public <T> void register(final Object o, final Class<T> identifier) {
		if(!o.getClass().isAssignableFrom(identifier)) {
			logging.error("The Object " + o.getClass() + " is not assignable from " + identifier);
		}
		synchronized (mapping) {
			mapping.put(identifier, o);
		}
	}

	@Override
	public <T> void unregister(final T t) {
		synchronized (mapping) {
			mapping.remove(t.getClass());
		}
	}

	@Override
	public void run(final RemoteAccessCommunicationModelRequest request, final Connection connection) {
		Class clazz = request.getClazz();
		Object o;
		synchronized (mapping) {
			o = mapping.get(clazz);
		}
		if(o == null) {
			throw new IllegalArgumentException("Nothing is registered for " + request.getClazz());
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
