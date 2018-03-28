package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;

/**
 * This EmptySynchronize is an NullObject for Synchronize.
 * <p>
 * This is non-blocking.
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
public class EmptySynchronize implements Synchronize {

	/**
	 * This is empty, so that nothing happens if it is called, to suit the empty-null-object nature
	 */
	@Override
	public void error() {
	}

	/**
	 * This is empty, so that nothing happens if it is called, to suit the empty-null-object nature
	 */
	@Override
	public void goOn() {
	}

	/**
	 * This is empty, so that nothing happens if it is called, to suit the empty-null-object nature
	 */
	@Override
	public void reset() {
	}

	/**
	 * This is empty, so that nothing happens if it is called, to suit the empty-null-object nature
	 */
	@Override
	public void synchronize() {
	}
}
