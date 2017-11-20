package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;

import java.io.IOException;

public interface Connector<Factory, Return> {

	/**
	 * The call of this method, results in the creation of the {@link Return} parameter, based upon the {@link Factory}
	 * parameter
	 *
	 * @param factory the creation of the {@link Return}
	 * @return a new instance of {@link Return}
	 * @throws IOException          if the Connection, using the factory fails
	 * @throws StartFailedException if anything else fails.
	 */
	Return establishConnection(final Factory factory) throws IOException, StartFailedException;

	/**
	 * The call of this method, results in the creation of the {@link Return} parameter, based upon the {@link Factory}
	 * parameter
	 * <p>
	 * Unlike the {@link #establishConnection(Object)} method, this method will create a new Connection identified with
	 * the provided key.
	 *
	 * @param key     the Class, which should identify the newly created Connection
	 * @param factory the creation of the {@link Return}
	 * @return a new instance of {@link Return}
	 * @throws IOException          if the Connection, using the factory fails
	 * @throws StartFailedException if anything else fails.
	 */
	Return establishConnection(final Class key, final Factory factory) throws IOException, StartFailedException;

	/**
	 * This Method might be overridden, to signal the Connector to shutDown any internal components.
	 *
	 * @throws IOException if something goes wrong during shutdown
	 */
	default void shutDown() throws IOException {
	}

}
