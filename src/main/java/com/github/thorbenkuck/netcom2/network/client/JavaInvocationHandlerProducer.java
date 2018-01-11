package com.github.thorbenkuck.netcom2.network.client;

import java.lang.reflect.InvocationHandler;
import java.util.UUID;

public class JavaInvocationHandlerProducer implements InvocationHandlerProducer {

	private final Sender sender;
	private final RemoteAccessBlockRegistration remoteAccessBlockRegistration;

	public JavaInvocationHandlerProducer(final Sender sender, final RemoteAccessBlockRegistration remoteAccessBlockRegistration) {
		this.sender = sender;
		this.remoteAccessBlockRegistration = remoteAccessBlockRegistration;
	}

	@Override
	public InvocationHandler produce(final UUID uuid, final Class<?> clazz) {
		return new JavaRemoteInformationInvocationHandler(sender, remoteAccessBlockRegistration, clazz, uuid);
	}
}
