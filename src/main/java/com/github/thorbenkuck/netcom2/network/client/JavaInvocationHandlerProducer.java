package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.UUID;

/**
 * This InvocationHandlerProducer produces JavaRemoteInformationInvocationHandler.
 * <p>
 * This Class is very likely to be subject to change! Keep that in mind when using it!
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.client.JavaInvocationHandlerProducerTest")
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
