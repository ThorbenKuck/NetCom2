package com.github.thorbenkuck.netcom2.network.shared;

/**
 * This functional interface is used to synchronize asynchronous processes.
 * <p>
 * Calling the {@link Awaiting#synchronize()} method, will block the current Thread, until the parallel operation is finished.
 * <p>
 * The object, that creates this element will, in some form or another create a thread, that does stuff or at least have
 * a Thread finish something asynchronously. As soon, as the procedure that should be finished is finished, the
 * implementation of this interface will let the synchronize method continue and thereby release all waiting Threads.
 *
 * @version 1.0
 * @since 1.0
 * @see com.github.thorbenkuck.netcom2.network.synchronization.DefaultSynchronize
 * @see AbstractSynchronize
 */
@FunctionalInterface
public interface Awaiting {

	/**
	 * This method blocks the current Thread, until the parallel operation is finished.
	 *
	 * @throws InterruptedException if the blocking-mechanism is interrupted in some form or another
	 */
	void synchronize() throws InterruptedException;

}
