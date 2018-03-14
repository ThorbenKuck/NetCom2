package com.github.thorbenkuck.netcom2.network.client;

import java.util.UUID;

public interface InvocationHandlerProducer {

	<T> JavaRemoteInformationInvocationHandler<T> produce(final UUID uuid, final Class<T> clazz);
}
