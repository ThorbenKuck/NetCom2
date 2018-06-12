package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.Experimental;

@Deprecated
public interface RemoteObjectAccess {

	/**
	 * Returns the internally maintained {@link RemoteObjectFactory}.
	 * <p>
	 * This method will never return null.
	 *
	 * @return the internally maintained instance of a RemoteObjectFactory.
	 */
	RemoteObjectFactory getRemoteObjectFactory();

	/**
	 * The call of this Method will result in a callable instance of the provided <code>class</code>.
	 * <p>
	 * This instance will be delegating the query back to the Server and return any result and throw any Throwable back
	 * from the Server.
	 * <p>
	 * This Method will potentially be deprecated in the future.
	 * <p>
	 * Use {@link ClientStart#getRemoteObjectFactory()} instead
	 * <p>
	 *
	 * @param clazz the Class of the Object that should be created
	 * @param <T>   the Type, identified by the provided class
	 * @return an callable instance of the type <code>T</code>
	 * @see RemoteObjectFactory
	 */
	@Experimental
	default <T> T getRemoteObject(Class<T> clazz) {
		return getRemoteObjectFactory().create(clazz);
	}

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
