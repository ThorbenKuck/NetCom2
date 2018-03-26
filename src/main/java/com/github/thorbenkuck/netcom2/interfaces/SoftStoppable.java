package com.github.thorbenkuck.netcom2.interfaces;

/**
 * This Method defines a stoppable Action
 * <p>
 * You might create a class, that is inherited by {@link Runnable} and SoftStoppable to create a Runnable that you
 * can stop using the {@link #softStop()} Method. So you do not need to hard-stop the Thread it is running in.
 *
 * @version 1.0
 * @since 1.0
 */
public interface SoftStoppable {

	/**
	 * This Method will stop the internal Mechanisms without stopping the thread it is running within.
	 * <p>
	 * The internal Mechanism should therefor depend on the {@link #running()} method. And the {@link #running()} method
	 * should return false, once this method is called.
	 */
	void softStop();

	/**
	 * Defines, whether or not the inheriting class is Running or not
	 *
	 * @return true, if {@link #softStop()} was not called yet, else false
	 */
	boolean running();
}
