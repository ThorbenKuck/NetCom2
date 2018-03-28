package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.Experimental;
import com.github.thorbenkuck.netcom2.annotations.rmi.RegistrationOverrideProhibited;
import com.github.thorbenkuck.netcom2.network.client.RemoteObjectAccess;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationResponse;

/**
 * Any Implementation of this Interface will handle the Registration of RemoteObjects at the Server-Side and therefore the delegation and Handling
 * of {@link RemoteAccessCommunicationRequest}s
 * <p>
 * Internally it holds Objects, that are responsible to be called when ever an Object, created with {@link RemoteObjectAccess}
 * is created. It contains methods to register and unregister Objects, identified by Classes. However, the provided classes
 * have to be assignable from the provided Object.
 *
 * @version 1.0
 * @since 1.0
 */
public interface RemoteObjectRegistration {

	/**
	 * This call will register the given Object, identified by its class.
	 * <p>
	 * Be careful with this call. If you want some class to be registered by its super-class or by any of its interfaces,
	 * use {@link #register(Object, Class[])} or {@link #hook(Object)}. In most cases
	 *
	 * @param object The object that should be registered
	 */
	@Experimental
	void register(Object object);

	/**
	 * This call will register the given Object, identified by ALL given Classes.
	 * <p>
	 * This call does not check, whether or not the class of the Object is contained within the Array of Classes!
	 * Further, this by calling this Method it is not checked whether or not all public declared interfaces ar contained withing <code>identifier</code>
	 * <p>
	 * To put it simple, the only thing that this method does, is to override any given instance with the declared classes
	 * <p>
	 * However, any class within <code>identifier</code> must be assignable from the provided object
	 *
	 * @param o          The Object
	 * @param identifier the identifiers
	 */
	@Experimental
	void register(Object o, Class<?>... identifier);

	/**
	 * Register the provided Object by all its class and all declared interfaces.
	 * <p>
	 * This Method will register the {@code object} by its class as well as by all declared interfaces. So registering the following class:
	 * <p>
	 * <code>
	 * class Foo implements Serializable, Runnable {
	 * }
	 * </code>
	 * <p>
	 * by stating {@code RemoteObjectRegistration#hook(new Foo())} will register the instance to <code>Foo.class</code>, <code>Serializable.class</code> and <code>Runnable.class</code> and will be called if one of those is requested to run.
	 * <p>
	 * This WILL override any previously set registrations!
	 *
	 * @param object the Object
	 * @throws IllegalArgumentException if the Object is null
	 * @see #register(Object)
	 * @see #register(Object, Class[])
	 */
	@Experimental
	void hook(Object object);

	/**
	 * Unregisters an given Object, identified by its class.
	 *
	 * @param object the object that should be unregistered
	 */
	@Experimental
	void unregister(Object object);

	/**
	 * This call will unregisters the given Object, identified by ALL given Classes.
	 * <p>
	 * This call does not check, whether or not the class of the Object is contained within the Array of Classes!
	 * Further, this by calling this Method it is not checked whether or not all public declared interfaces ar contained withing <code>identifier</code>
	 * <p>
	 * To put it simple, the only thing that this method does, is to override any given instance with the declared classes
	 * <p>
	 * However, any the Object registered to those identifiers has to be equal to the provided one. Therefor, if any other
	 * procedure overwrote the Object that where previously saved, this method will ignore this registration.
	 *
	 * @param object      the Object, that should be registered internally
	 * @param identifiers the identifiers to check for.
	 */
	@Experimental
	void unregister(Object object, Class... identifiers);

	/**
	 * This call unregisters any Objects, registered at any way internally.
	 * <p>
	 * It makes no checks whether or not the provided objects are correct.
	 * If you want to only unregister certain objects, the use of {@link #unregister(Object, Class[])} is to be preferred.
	 * If you want to clear out, any instance of an Object, the use of {@link #unhook(Object)} is to be preferred.
	 *
	 * @param identifier all identifiers, that should be unregistered.
	 */
	@Experimental
	void unregister(Class... identifier);

	/**
	 * Similarly to Hook, this will search for all public interfaces, declared by the direct class of the Object and unregister them
	 *
	 * @param object the Object that should be unhooked
	 * @see #hook(Object)
	 */
	@Experimental
	void unhook(Object object);

	/**
	 * Clears out all saved instances.
	 * <p>
	 * This means, also Classes annotated with {@link RegistrationOverrideProhibited}
	 * will be cleared!
	 */
	@Experimental
	void clear();

	/**
	 * This call executes an {@link RemoteAccessCommunicationRequest} with the provided instances internally, then return
	 * a {@link RemoteAccessCommunicationResponse}.
	 * <p>
	 * This method will search internally for the set instance, according to the {@link RemoteAccessCommunicationRequest#clazz},
	 * which identifies the Object that should be called. If it finds an corresponding Class, it searches the Class via
	 * reflection for the provided {@link RemoteAccessCommunicationRequest#methodName}, which matches the {@link RemoteAccessCommunicationRequest#parameters}.
	 * if it can find any matching method, it will execute the first one and generate the Results of that Method.
	 * <p>
	 * Any Exception thrown, will be cached and sent back to the Client. This means, the StackTrace will be the StackTrace
	 * which contains information about the Server! Use the {@link com.github.thorbenkuck.netcom2.annotations.rmi.IgnoreRemoteExceptions}
	 * annotation to suppress this behaviour.
	 * <p>
	 * If this Annotation is present, it will substitute the throwable with null and return null, even if an exception was thrown.
	 * In any case the Exception will be logged using the {@link com.github.thorbenkuck.netcom2.logging.NetComLogging}.
	 * <p>
	 * If no Exception is encountered, the generated Result will be encapsulated within the {@link RemoteAccessCommunicationResponse}
	 *
	 * @param request the result, that was received over the Network
	 * @return the computed RemoteAccessCommunicationResponse, that should be send over the Network
	 */
	@Experimental
	RemoteAccessCommunicationResponse run(final RemoteAccessCommunicationRequest request);

}
