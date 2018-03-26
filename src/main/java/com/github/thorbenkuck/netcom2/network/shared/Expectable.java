package com.github.thorbenkuck.netcom2.network.shared;

/**
 * Latches for the Receiving of an specific Object
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface Expectable {

	/**
	 * Allows to wait for something.
	 * <p>
	 * The something is defined by its class.
	 *
	 * @param clazz the class, that defines the type of the object to wait for
	 * @throws InterruptedException if Thread.interrupt is called while waiting for the Object
	 */
	void andWaitFor(final Class clazz) throws InterruptedException;

}
