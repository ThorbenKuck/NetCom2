package com.github.thorbenkuck.netcom2.network.client;

import java.util.UUID;

/**
 * This interface is like a Factory, for creating a {@link JavaRemoteInformationInvocationHandler}.
 * <p>
 * The future of this interface is uncertain, since the {@link JavaRemoteInformationInvocationHandler} is bound to Java
 * and this advanced RMI API should be accessible across most programming languages.
 *
 * @version 1.0
 * @since 1.0
 */
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
