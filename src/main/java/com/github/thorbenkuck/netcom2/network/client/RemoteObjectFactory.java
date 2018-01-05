package com.github.thorbenkuck.netcom2.network.client;

import java.lang.reflect.Proxy;
import java.util.UUID;

class RemoteObjectFactory {

	private final Sender sender;
	private final RemoteAccessBlockRegistration remoteAccessBlockRegistration = new RemoteAccessBlockRegistration();

	RemoteObjectFactory(final Sender sender) {
		this.sender = sender;
	}

	@SuppressWarnings ("unchecked")
	<T> T createRemoteObject(Class<T> clazz) {
		UUID uuid;
		synchronized (this) {
			uuid = UUID.randomUUID();
		}

		return (T) Proxy.newProxyInstance(RemoteObjectFactory.class.getClassLoader(),
				new Class[]{clazz},
				new RemoteInformationInvocationHandler(sender, remoteAccessBlockRegistration, clazz, uuid));
	}

	public RemoteAccessBlockRegistration getRemoteAccessBlockRegistration() {
		return remoteAccessBlockRegistration;
	}

}
