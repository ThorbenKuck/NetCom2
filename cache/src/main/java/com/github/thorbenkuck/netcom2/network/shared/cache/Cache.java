package com.github.thorbenkuck.netcom2.network.shared.cache;

import com.github.thorbenkuck.netcom2.interfaces.Mutex;

import java.util.Optional;

public interface Cache extends Mutex {

	/**
	 * Instantiates a new Cache.
	 * <p>
	 * This instance is hidden. Do not cast, nor try to instantiate the implementation by yourself.
	 * <p>
	 * The implementation is an easy target to be object to change.
	 *
	 * @return a new Cache instance
	 */
	static Cache open() {
		return new NativeCache();
	}

	/**
	 * Updates an saved instance within the Cache.
	 * <p>
	 * If the Object-type (Class) of your provided Object is set inside the Cache, this will update the previously
	 * saved instance with the new provided one.
	 * <p>
	 * If it is not set, nothing (apart from logging an warning) will happen.
	 *
	 * @param object the Object that should be updated
	 */
	void update(final Object object);

	/**
	 * Sets an new instance into the Cache.
	 * <p>
	 * If the Object-type (Class) of your provided Object is not yet set inside the Cache, this will set the provided
	 * instance.
	 * <p>
	 * If it is already set, nothing (apart from logging an warning) will happen.
	 *
	 * @param object the Object that should be updated
	 */
	void addNew(final Object object);

	/**
	 * Sets an new instance into the Cache, ignoring previously set instances.
	 * <p>
	 * If the Object-type (Class) of your provided Object is not yet set inside the Cache, this will set the provided
	 * instance.
	 * <p>
	 * If the Object-type (Class) of your provided Object is set inside the Cache, this will update the previously
	 * saved instance with the new provided one.
	 *
	 * @param object the Object that should be updated
	 */
	void addAndOverride(final Object object);

	/**
	 * Removes whatever instance is associated with the provided Class.
	 *
	 * @param clazz the class of the Object you want to remove from this Cache
	 */
	void remove(final Class clazz);

	/**
	 * Returns the internally maintained instance for the provided class.
	 *
	 * @param clazz the class you want want to get the instance for
	 * @param <T>   the type, defined by the class
	 * @return an Optional, empty if nothing is set.
	 */
	<T> Optional<T> get(final Class<T> clazz);

	/**
	 * Returns, whether an instance exists for the provided class or not.
	 *
	 * @param clazz the class you want to check for
	 * @return true, if the type is set, else false
	 */
	boolean isSet(final Class<?> clazz);

	/**
	 * Adds a {@link CacheObserver} to this Cache.
	 * <p>
	 * Any set CacheObserver will be notified whenever the call of {@link #update(Object)}, {@link #addNew(Object)},
	 * {@link #addAndOverride(Object)} or {@link #remove(Class)} successfully passed for the Object you want to listen to.
	 * <p>
	 * Those callbacks are a great way (and in fact needed for the Registration mechanism) of getting notified.
	 *
	 * @param cacheObserver the CacheObserver, that should listen to this cache
	 * @param <T>           the Type of that CacheObserver
	 */
	<T> void addCacheObserver(final CacheObserver<T> cacheObserver);

	/**
	 * Removes a {@link CacheObserver} from this Cache
	 *
	 * @param cacheObserver the CacheObserver, that should be removed
	 * @param <T>           the Type of that CacheObserver
	 */
	<T> void removeCacheObserver(final CacheObserver<T> cacheObserver);

	/**
	 * Adds an {@link GeneralCacheObserver} to this Cache.
	 * <p>
	 * GeneralCacheObserver will be notified whenever any object changes within this Cache.
	 * <p>
	 * It is recommended to use {@link #addCacheObserver(CacheObserver)}, instead of this method.
	 *
	 * @param observer the Observer, that should listen to this Cache
	 */
	void addGeneralObserver(final GeneralCacheObserver observer);

	/**
	 * Removes a {@link GeneralCacheObserver} from this Cache
	 *
	 * @param observer the Observer, that should be removed
	 */
	void removeGeneralObserver(final GeneralCacheObserver observer);

	/**
	 * Deletes all Observers.
	 */
	void clearObservers();

	/**
	 * Returns the amount of Observers, that are saved within this Cache
	 *
	 * @return the amount of Observers in this Cache
	 */
	int countObservers();

	/**
	 * Clears the internally saved Observers as well as all internally saved Objects.
	 */
	void reset();
}
