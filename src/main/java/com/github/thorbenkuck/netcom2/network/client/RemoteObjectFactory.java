package com.github.thorbenkuck.netcom2.network.client;

public interface RemoteObjectFactory {

	/**
	 * Sets the default Runnable fallback, if the requested Class is not registered.
	 *
	 * This allows you, to say what to do, if the RemoteObject is not registered.
	 *
	 * The Runnable will be executed, if and only if the requested Object is not registered i.e. if an {@link com.github.thorbenkuck.netcom2.exceptions.RemoteObjectNotRegisteredException}
	 * is thrown at the Server.
	 *
	 * @param runnable
	 */
	void addFallback(Runnable runnable);

	/**
	 * Adds an callback, mapped to a certain Class.
	 *
	 * If the provided Class is requested, this fallback runnable is used preferred over {@link #addFallback(Runnable)}
	 *
	 * @param runnable
	 * @param clazz
	 */
	void addFallback(Class<?> clazz, Runnable runnable);

	<T, S extends T> void addFallback(Class<T> clazz, S instance);

	<T> T create(Class<T> type);

	<T> T create(Class<T> type, Runnable customFallback);

	<T> T create(Class<T> type, T fallbackMethod);

}
