package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelRequest;

public interface RemoteObjectRegistration {

	/**
	 * This call will register the given Object, identified by its class.
	 *
	 * @param object The object that should be registered
	 */
	void register(Object object);

	/**
	 * This call will register the given Object, identified by ALL given Classes.
	 *
	 * This call does not check, whether or not the class of the Object is contained within the Array of Classes!
	 *
	 * @param o The Object
	 * @param identifier the identifiers
	 */
	void register(Object o, Class<?>... identifier);

	/**
	 * Register the provided Object by all its class and all declared interfaces.
	 *
	 * This Method will register the {@code object} by its class as well as by all declared interfaces. So registering the following class:
	 *
	 * {@code
	 * class Foo implements Serializable, Runnable {
	 *
	 * }
	 * }
	 *
	 * by stating {@code RemoteObjectRegistration#hook(new Foo())} will register the instance to Foo.class, Serializable.class and Runnable.class and will be called if one of those is requested to run.
	 *
	 * This WILL override any previously set registrations!
	 *
	 * @throws IllegalArgumentException if the Object is null
	 * @param object the Object
	 */
	void hook(Object object);

	void unregister(Object object);

	Object run(final Connection connection, final Session session, final RemoteAccessCommunicationModelRequest request) throws RemoteRequestException;

}
