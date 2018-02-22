package com.github.thorbenkuck.netcom2.network.client;

import java.util.UUID;

public interface InvocationHandlerProducer {

	RemoteObjectHandler produce(final UUID uuid, final Class<?> clazz);
}
