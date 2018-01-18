package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest;

/**
 * Any Implementation of this Interface will handle the Registration of RemoteObjects at the Server-Side and therefor the delegation and Handling
 * of {@link RemoteAccessCommunicationRequest}s
 *
 * Internally it holds Objects, that are responsible to be called when ever an Object, created with {@link RemoteObjectAccess}
 * is created. It contains methods to register and unregister Objects, identified by Classes. However, the provided classes
 * have to be assignable from the provided Object.
 */
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

	/**
	 * This call will unregisters the given Object, identified by ALL given Classes.
	 *
	 * This call does not check, whether or not the class of the Object is contained within the Array of Classes!
	 * Further, this by calling this Method it is not checked whether or not all public declared interfaces ar contained withing <code>identifier</code>
	 *
	 * To put it simple, the only thing that this method does, is to override any given instance with the declared classes
	 *
	 * However, any the Object registered to those identifiers has to be equal to the provided one. Therefor, if any other
	 * procedure overwrote the Object that where previously saved, this method will ignore this registration.
	 *
	 * @param object the Object, that should be registered internally
	 * @param identifiers the identifiers to check for.
	 */
	void unregister(Object object, Class... identifiers);

	/**
	 * This call unregisters any Objects, registered at any way internally.
	 *
	 * It makes no checks whether or not the provided objects are correct.
	 * If you want to only unregister certain objects, the use of {@link #unregister(Object, Class[])} is to be preferred.
	 * If you want to clear out, any instance of an Object, the use of {@link #unhook(Object)} is to be preferred.
	 *
	 * @param identifier all identifiers, that should be unregistered.
	 */
	void unregister(Class... identifier);

	/**
	 * Likewise to Hook, this will search for all public interfaces, declared by the direct class of the Object and unregister them
	 *
	 * @param object
	 */
	void unhook(Object object);

	/**
	 * Clears out all saved instances.
	 *
	 * This means, also Classes annotated with {@link com.github.thorbenkuck.netcom2.annotations.remoteObjects.OverrideProhibited}
	 * will be cleared!
	 */
	void clear();

	Object run(final RemoteAccessCommunicationRequest request) throws RemoteRequestException;

}
