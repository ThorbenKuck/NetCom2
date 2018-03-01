package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.annotations.Experimental;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.InvocationHandlerProducer;

/**
 * This interface provides an way of getting RemoteObjects.
 * <p>
 * The {@link ClientStart} for example implements this method. So you
 * may get any RemoteObject by stating
 * <p>
 * <code>
 * class Example {
 * private ClientStart clientStart;
 *
 * ...
 *
 * public void run() {
 * TestObject testObject = clientStart.getRemoteObject(TestObject.class);
 * }
 * }
 * </code>
 * <p>
 * It returns an actual, callable instance of the provided Class. However, the Method-call will be delegated to the Server.
 * The Server then calculates results based upon the real Object, registered at the {@link RemoteObjectRegistration} and
 * then return this Result back to the Client.
 * <p>
 * Further any Throwable encountered while running the real Object will be pinged back to the executing client.
 *
 * @see com.github.thorbenkuck.netcom2.annotations.remoteObjects.IgnoreRemoteExceptions
 * @see com.github.thorbenkuck.netcom2.annotations.remoteObjects.SingletonRemoteObject
 * @see com.github.thorbenkuck.netcom2.network.client.RemoteObjectHandler
 */
public interface RemoteObjectAccess {

	/**
	 * The call of this Method will result in an callable instance of the provided <code>class</code>.
	 * <p>
	 * This instance will be delegating the quarry back to the Server and return any result and throw any Throwable back
	 * from the Server.
	 *
	 * @param clazz the Class of the Object that should be created
	 * @param <T>   the Type, identified by the provided class
	 * @return an callable instance of the type <code>T</code>
	 */
	@Experimental
	<T> T getRemoteObject(Class<T> clazz);

	/**
	 * This Method changes the {@link InvocationHandlerProducer} set internally.
	 * <p>
	 * The {@link InvocationHandlerProducer} creates a new Instance if {@link #getRemoteObject(Class)} is called.
	 * By changing this {@link InvocationHandlerProducer}, you can provide a custom {@link com.github.thorbenkuck.netcom2.network.client.RemoteObjectHandler}
	 * to be used internally
	 *
	 * @param invocationHandlerProducer the {@link InvocationHandlerProducer} that should create the {@link com.github.thorbenkuck.netcom2.network.client.RemoteObjectHandler}
	 * @see InvocationHandlerProducer
	 */
	@Experimental
	void updateRemoteInvocationProducer(InvocationHandlerProducer invocationHandlerProducer);

}
