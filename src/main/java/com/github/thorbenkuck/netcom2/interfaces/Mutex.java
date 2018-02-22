package com.github.thorbenkuck.netcom2.interfaces;

/**
 * This interface provides an mechanism to acquire any Object at runtime to ensure Thread-Safety.
 *
 * By calling <code>acquire</code>, the object will block any other <code>acquire</code> calls until release is called.
 *
 * It emulates a {@link java.util.concurrent.Semaphore}. Therefor, it can be realized internally with a Semaphore.
 * It encapsulates an Semaphore. Therefor the Synchronization Mechanism can be changed easily.
 */
public interface Mutex {

	/**
	 * Acquires access over the object. If any other Object has access over the given Object, this Methods waits until
	 * the current owner calls release.
	 *
	 * @throws InterruptedException if the waiting takes to long.
	 */
	void acquire() throws InterruptedException;

	/**
	 * Releases the access over the Object and invoking any waiting Threads.
	 */
	void release();

}
