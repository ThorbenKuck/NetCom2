package com.github.thorbenkuck.netcom2.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

/**
 * This Utility Class defines some methods, that are commonly used within NetCom2
 * <p>
 * Since those checks are stateless, they are static.
 * Thread-Safety should not be necessary, else whatever needs Thread-Safety does not belong here
 * <p>
 * Since this class is used mainly internally, it should not be used outside of the internal project.
 * If you want to use it anyways, use it with care! This class is highly likely to be subject of change!
 *
 * @version 1.0
 * @since 1.0
 */
public class NetCom2Utils {

	/*
	 * The following is used internally in this class only
	 */
	private static final Semaphore synchronization = new Semaphore(1);

	/**
	 * This checks for null. If the provided Object <code>o</code> is found to be null, a {@link NullPointerException}
	 * will be thrown.
	 *
	 * @param o the Object, that should be tested against null
	 * @see #assertNotNull(Object...)
	 */
	public static void assertNotNull(final Object o) {
		if (o == null) {
			throw new NullPointerException();
		}
	}

	/**
	 * Like {@link #assertNotNull(Object)}, this method checks for null.
	 * <p>
	 * The difference is, that you may add any Number of Objects to check here.
	 *
	 * @param objects an array of Objects, that should be checked against null
	 * @see #assertNotNull(Object)
	 */
	public static void assertNotNull(final Object... objects) {
		for (final Object object : objects) {
			assertNotNull(object);
		}
	}

	/**
	 * This Method checks, if the given Object is null.
	 * <p>
	 * Other than {@link #assertNotNull(Object)} and  {@link #assertNotNull(Object...)}, this method will throw an
	 * IllegalArgumentException.
	 * <p>
	 * So basically this Method checks, for illegal arguments, which in fact are null.
	 *
	 * @param object the Object that should be checked against null
	 * @see #parameterNotNull(Object...)
	 */
	public static void parameterNotNull(final Object object) {
		if (object == null) {
			throw new IllegalArgumentException("Null is not a valid parameter!");
		}
	}

	/**
	 * This Method checks, if the given Objects are null.
	 * <p>
	 * Other than {@link #assertNotNull(Object)} and  {@link #assertNotNull(Object...)}, this method will throw an
	 * IllegalArgumentException.
	 * <p>
	 * So basically this Method checks, for illegal arguments, which in fact are null.
	 *
	 * @param objects an array of Objects that should be checked against null
	 * @see #parameterNotNull(Object)
	 */
	public static void parameterNotNull(final Object... objects) {
		if (objects == null)
			throw new IllegalArgumentException("Null is not a valid parameter!");
		for (final Object object : objects) {
			parameterNotNull(object);
		}
	}

	/**
	 * Executes an runnable, but synchronized synchronizes over an Semaphore.
	 * <p>
	 * Stays on current Thread
	 *
	 * @param runnable the runnable, that should be executed synchronized
	 */
	public static void runSynchronized(final Runnable runnable) {
		parameterNotNull(runnable);
		try {
			synchronization.acquire();
			runnable.run();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		} finally {
			synchronization.release();
		}
	}

	/**
	 * Creates an ThreadSafe Iterator over the provided Collection
	 *
	 * @param of  the Collection, that the Iterator should be iterated over
	 * @param <T> the type of the Collection
	 * @return an ThreadSafe iterator
	 * @deprecated use the {@link #iterator(Collection)} method. The naming is more easy to understand
	 */
	@Deprecated
	public static <T> Iterator<T> createAsynchronousIterator(final Collection<T> of) {
		parameterNotNull(of);
		return new AsynchronousIterator<>(of);
	}

	/**
	 * Creates an ThreadSafe Iterator over the provided Collection
	 * <p>
	 * If true is passed in as the <code>removeAllowed</code> the Iterator will use its {@link Iterator#remove()} on the
	 * provided Collection.
	 *
	 * @param of            the Collection, that the Iterator should be iterated over
	 * @param removeAllowed whether or not the iterator should be allowed to remove elements from the provided Collection
	 * @param <T>           the type of the Collection
	 * @return an ThreadSafe iterator
	 * @deprecated use the {@link #iterator(Collection, boolean)} method. The naming is more easy to understand
	 */
	@Deprecated
	public static <T> Iterator<T> createAsynchronousIterator(final Collection<T> of, boolean removeAllowed) {
		parameterNotNull(of);
		return new AsynchronousIterator<>(of, removeAllowed);
	}

	/**
	 * Creates an ThreadSafe Iterator over the provided Collection
	 *
	 * @param of  the Collection, that the Iterator should be iterated over
	 * @param <T> the type of the Collection
	 * @return an ThreadSafe iterator
	 */
	public static <T> Iterator<T> iterator(final Collection<T> of) {
		parameterNotNull(of);
		return new AsynchronousIterator<>(of);
	}

	/**
	 * Creates an ThreadSafe Iterator over the provided Collection
	 * <p>
	 * If true is passed in as the <code>removeAllowed</code> the Iterator will use its {@link Iterator#remove()} on the
	 * provided Collection.
	 *
	 * @param of            the Collection, that the Iterator should be iterated over
	 * @param removeAllowed whether or not the iterator should be allowed to remove elements from the provided Collection
	 * @param <T>           the type of the Collection
	 * @return an ThreadSafe iterator
	 */
	public static <T> Iterator<T> iterator(final Collection<T> of, boolean removeAllowed) {
		parameterNotNull(of);
		return new AsynchronousIterator<>(of, removeAllowed);
	}
}