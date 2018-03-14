package com.github.thorbenkuck.netcom2.network.shared;

/**
 * This functional Interface is used to synchronize asynchronous processes.
 * <p>
 * Calling the {@link Awaiting#synchronize()} method, will block the current thread, until the parallel operation is finished.
 * <p>
 * The object, that creates this element will, in some form or another create a thread, that does stuff. As soon, as it
 * is sendOverNetwork, the implementation of this interface will, in some form, let the synchronize method continue.
 */
@FunctionalInterface
public interface Awaiting {

	/**
	 * This method blocks the current Thread, until the parallel Operation is finished.
	 *
	 * @throws InterruptedException if the blocking-mechanism is interrupted in some form or another
	 */
	void synchronize() throws InterruptedException;

}
