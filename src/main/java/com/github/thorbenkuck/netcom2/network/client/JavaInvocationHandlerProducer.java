package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> JavaRemoteInformationInvocationHandler<T> produce(final UUID uuid, final Class<T> clazz) {
		NetCom2Utils.parameterNotNull(clazz, uuid);
		return new JavaRemoteInformationInvocationHandler<>(sender, remoteAccessBlockRegistration, clazz, uuid);
	}
}
