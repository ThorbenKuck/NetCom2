package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.interfaces.Module;

public interface RemoteObjectFactory extends Module<ClientStart> {

	static RemoteObjectFactory open(ClientStart clientStart) {
		NativeRemoteObjectFactory remoteObjectFactory = new NativeRemoteObjectFactory();
		remoteObjectFactory.setup(clientStart);

		return remoteObjectFactory;
	}

	/**
	 * Sets the default Runnable fallback, if the requested Class is not registered.
	 * <p>
	 * This allows you, to say what to do, if the RemoteObject is not registered.
	 * <p>
	 * The Runnable will be executed, if and only if the requested Object is not registered i.e. if a {@link com.github.thorbenkuck.netcom2.exceptions.RemoteObjectNotRegisteredException}
	 * is thrown on the Server. Else, the received Exception will be rethrown, encapsulated in a {@link com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException}.
	 *
	 * @param runnable the runnable, that should be executed, if the requested Class is not present.
	 */
	void setDefaultFallback(Runnable runnable);

	/**
	 * Adds a fallback, mapped to a certain Class.
	 * <p>
	 * If a RemoteObject of the type <code>clazz</code> is not registered, this runnable will be executed.
	 * <p>
	 * If the provided Class is requested, this fallback runnable is used preferred over {@link #setDefaultFallback(Runnable)}.
	 * Otherwise, the behaviour is the same.
	 *
	 * @param runnable the runnable, that should be executed, if the requested class is not present
	 * @param clazz    the class, that was requested
	 */
	void setFallback(Class<?> clazz, Runnable runnable);

	/**
	 * Adds a fallback instance, that should be executed if the remote-object is not registered.
	 * <p>
	 * If the provided Class is requested, this fallback runnable is used preferred over {@link #setFallback(Class, Runnable)}.
	 * Otherwise, the behaviour is the same.
	 *
	 * @param clazz    the class, that was requested
	 * @param instance the instance, that should be used if the remote object is not registered.
	 * @param <T>      the Type of the requested remote object
	 * @param <S>      the Type of the fallback instance
	 */
	<T, S extends T> void setFallbackInstance(Class<T> clazz, S instance);

	/**
	 * Creates the RemoteObject.
	 * <p>
	 * This utilizes the set Fallbacks, if the requested Class is not present.
	 *
	 * @param type the Class of the requested RemoteObject
	 * @param <T>  the Type of the requested RemoteObject
	 * @return an proxy instance for the requested type
	 * @see #setDefaultFallback(Runnable)
	 * @see #setFallback(Class, Runnable)
	 * @see #setFallbackInstance(Class, Object)
	 */
	<T> T create(Class<T> type);

	/**
	 * Creates the RemoteObject.
	 * <p>
	 * Calling this method will ignore previously set instances of fallbacks.
	 * <p>
	 * So, if any callbacks are set, this method will ignore them and set the provided runnable to be used, if the Object
	 * is not registered at the Server-side
	 *
	 * @param type the Class of the requested RemoteObject
	 * @param <T>  the Type of the requested RemoteObject
	 * @return an proxy instance for the requested type
	 * @see #setDefaultFallback(Runnable)
	 * @see #setFallback(Class, Runnable)
	 * @see #setFallbackInstance(Class, Object)
	 */
	<T> T create(Class<T> type, Runnable customFallback);

	/**
	 * Creates the RemoteObject.
	 * <p>
	 * Calling this method will ignore previously set instances of fallbacks.
	 * <p>
	 * So, if any callbacks are set, this method will ignore them and set the provided instance to be used, if the Object
	 * is not registered at the Server-side. Then, it will execute the requested Method on the provided <code>fallbackInstance</code>.
	 *
	 * @param type             the Class of the requested RemoteObject
	 * @param <T>              the Type of the requested RemoteObject
	 * @param fallbackInstance the instance that should be used, if the requested Object is not registered
	 * @return an proxy instance for the requested type
	 * @see #setDefaultFallback(Runnable)
	 * @see #setFallback(Class, Runnable)
	 * @see #setFallbackInstance(Class, Object)
	 */
	<T> T create(Class<T> type, T fallbackInstance);

	/**
	 * This method will produce an Proxy for the RemoteObject.
	 * <p>
	 * In contrast to the other create Methods, this will result in an Proxy with no fallback.
	 * <p>
	 * This means, if the requested Object is not registered at the Server-side, calling any method of the Proxy
	 * will result in an {@link com.github.thorbenkuck.netcom2.exceptions.RemoteObjectNotRegisteredException}.
	 * <p>
	 * If you plan on using the RemoteObjects in any Module, that does not know of the Network, this is not recommended.
	 *
	 * @param type the Class of the requested RemoteObject
	 * @param <T>  the Type of the requested RemoteObject
	 * @return an new Proxy class of the provided type.
	 */
	<T> T createWithoutFallback(Class<T> type);

}
