package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.util.UUID;

public class JavaInvocationHandlerProducer implements InvocationHandlerProducer {

	@APILevel
	private final Sender sender;
	@APILevel
	private final RemoteAccessBlockRegistration remoteAccessBlockRegistration;

	public JavaInvocationHandlerProducer(final Sender sender, final RemoteAccessBlockRegistration remoteAccessBlockRegistration) {
		this.sender = sender;
		this.remoteAccessBlockRegistration = remoteAccessBlockRegistration;
	}

	@Override
	public <T> JavaRemoteInformationInvocationHandler<T> produce(final UUID uuid, final Class<T> clazz) {
		return new JavaRemoteInformationInvocationHandler<>(sender, remoteAccessBlockRegistration, clazz, uuid);
	}
}
