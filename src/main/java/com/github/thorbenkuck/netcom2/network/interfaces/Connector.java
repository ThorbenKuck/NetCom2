package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;

import java.io.IOException;

/**
 * This interface connects an type <code>T</code> to an synchronization mechanism <code>S</code>
 *
 * @param <T> The input argument
 * @param <S> The output argument
 *
 * @version 1.0
 * @since 1.0
 */
public interface Connector<T, S> {

	/**
	 * The call of this method, results in the creation of the {@link S} parameter, based upon the {@link T}
	 * parameter
	 *
	 * @param t the creation of the {@link S}
	 * @return a new instance of {@link S}
	 * @throws IOException          if the Connection, using the factory fails
	 * @throws StartFailedException if anything else fails.
	 */
	S establishConnection(final T t) throws IOException, StartFailedException;

	/**
	 * The call of this method, results in the creation of the {@link S} parameter, based upon the {@link T}
	 * parameter
	 * <p>
	 * Unlike the {@link #establishConnection(Object)} method, this method will create a new Connection identified with
	 * the provided key.
	 *
	 * @param key the Class, which should identify the newly created Connection
	 * @param t   the creation of the {@link S}
	 * @return a new instance of {@link S}
	 * @throws IOException          if the Connection, using the factory fails
	 * @throws StartFailedException if anything else fails.
	 */
	S establishConnection(final Class key, final T t) throws IOException, StartFailedException;

	/**
	 * This Method might be overridden, to signal the Connector to shutDown any internal components.
	 * <p>
	 * By Default, this Method does nothing by default.
	 *
	 * @throws IOException if something goes wrong during shutdown
	 */
	default void shutDown() throws IOException {
	}

}
