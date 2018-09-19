package com.github.thorbenkuck.netcom2.network.client;

import java.util.UUID;

public interface InvocationHandlerProducer {

	/**
	 * Produces an {@link JavaRemoteInformationInvocationHandler}, based on the UUID and the Class
	 *
	 * @param uuid  the ID, that the JavaRemoteInformationInvocationHandler is associated with
	 * @param clazz the Class, which the JavaRemoteInformationInvocationHandler proxies
	 * @param <T>   the Type of the JavaRemoteInformationInvocationHandler, defined by the class
	 * @return a completely new instance of the JavaRemoteInformationInvocationHandler
	 */
	<T> JavaRemoteInformationInvocationHandler<T> produce(final UUID uuid, final Class<T> clazz);
}
