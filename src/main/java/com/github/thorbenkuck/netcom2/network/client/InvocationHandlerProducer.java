package com.github.thorbenkuck.netcom2.network.client;

import java.lang.reflect.InvocationHandler;
import java.util.UUID;

public interface InvocationHandlerProducer {

	InvocationHandler produce(final UUID uuid, final Class<?> clazz);
}
