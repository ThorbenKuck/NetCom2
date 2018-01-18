package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest;

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
	 * Further, this by calling this Method it is not checked whether or not all public declared interfaces ar contained withing <code>identifier</code>
	 *
	 * To put it simple, the only thing that this method does, is to override any given instance with the declared classes
	 *
	 * However, any class within <code>identifier</code> must be assignable from the provided object
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
	 * <code>
	 * class Foo implements Serializable, Runnable {
	 *
	 * }
	 * </code>
	 *
	 * by stating {@code RemoteObjectRegistration#hook(new Foo())} will register the instance to <code>Foo.class</code>, <code>Serializable.class</code> and <code>Runnable.class</code> and will be called if one of those is requested to run.
	 *
	 * This WILL override any previously set registrations!
	 *
	 * @see #register(Object)
	 * @see #register(Object, Class[])
	 * @throws IllegalArgumentException if the Object is null
	 * @param object the Object
	 */
	void hook(Object object);

	/**
	 * Unregisters an given Object, identified by its class.
	 *
	 * @param object the object that should be unregistered
	 */
	void unregister(Object object);

	void unregister(Object object, Class... identifiers);

	void unhook(Object object);

	Object run(final RemoteAccessCommunicationRequest request) throws RemoteRequestException;

}
