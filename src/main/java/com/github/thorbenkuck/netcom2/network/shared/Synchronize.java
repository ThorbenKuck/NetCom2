package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.network.synchronization.DefaultSynchronize;

/**
 * This Class is used for synchronization within NetCom2.
 * <p>
 * Since this is extending the {@link Awaiting}, you might expose an method that returns an Awaiting and maintain an
 * Synchronize internally.
 * <p>
 * For example:
 * <p>
 * <pre>
 * {@code
 * class ModuleInternal {
 *
 *     private final ExecutorService threadPool = Executors.newCachedThreadPool()
 *
 *     public Awaiting doSomethingAndAwaitFinished() {
 *         Synchronize synchronize = Synchronize.access();
 *         threadPool.submit(() -> {
 *             // some work.
 *             synchronize.goOn();
 *         }
 *         return synchronize;
 *     }
 * }
 *
 * class User {
 *
 *     private ModuleInternal internal = ...;
 *
 *     public void doSomething() {
 *         Awaiting awaiting = internal.doSomethingAndAwaitFinished();
 *         // Some other work.
 *         awaiting.synchronize();
 *     }
 *
 * }
 * }
 * </pre>
 * <p>
 * So, the User can do some separate work and at a certain Point, await that the doSomethingAndAwaitFinished finishes.
 *
 * @version 1.0
 * @since 1.0
 */
public interface Synchronize extends Awaiting {

	/**
	 * Creates a non-blocking, null-Object Synchronize
	 *
	 * @return a cached instance
	 */
	static Synchronize empty() {
		return SynchronizeCache.EMPTY_SYNCHRONIZE;
	}

	/**
	 * Creates a new, normal instance of an Synchronize.
	 *
	 * @return a completely new instance
	 */
	static Synchronize create() {
		return new DefaultSynchronize();
	}

	/**
	 * Checks, whether or not the given Synchronize is empty or not.
	 * <p>
	 * This call checks for same. The provided Synchronize has to be the same as the EmptySynchronize to return true.
	 *
	 * @param synchronize the Synchronize to check
	 * @return true, is the provided Synchronize is the EmptySynchronize, else false
	 */
	static boolean isEmpty(Synchronize synchronize) {
		return synchronize == SynchronizeCache.EMPTY_SYNCHRONIZE;
	}

	/**
	 * Checks, whether or not the given Awaiting is empty or not.
	 * <p>
	 * This call checks for same. The provided Awaiting has to be the same as the EmptySynchronize to return true.
	 *
	 * @param awaiting the Awaiting to check
	 * @return true, is the provided Awaiting is the EmptySynchronize, else false
	 */
	static boolean isEmpty(Awaiting awaiting) {
		return awaiting == SynchronizeCache.EMPTY_SYNCHRONIZE;
	}

	/**
	 * Can be called, if an error occurred.
	 */
	void error();

	/**
	 * Can be called, if the awaited procedure is finished.
	 * <p>
	 * Releases awaiting Threads.
	 */
	void goOn();

	/**
	 * Resets the instance of the Synchronize
	 */
	void reset();
}
