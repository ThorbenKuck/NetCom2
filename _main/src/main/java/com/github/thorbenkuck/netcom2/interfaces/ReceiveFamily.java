package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.shared.OnReceive;

public interface ReceiveFamily {

	/**
	 * If either {@link #beforeExecution()}, {@link OnReceive#accept(Object, Object)} or {@link #successfullyExecuted()}
	 * throws an exception, the execution is stopped and this method is called.
	 * <p>
	 * This means, if <code>beforeExecution</code> throws an Exception, <code>accept</code> is never called!
	 *
	 * @param e the encountered Exception.
	 */
	default void exceptionEncountered(Exception e) {
	}

	/**
	 * This method might be overridden, to be notified if this implementation was successfully executed.
	 * <p>
	 * By default, this does nothing.
	 */
	default void successfullyExecuted() {
	}

	/**
	 * This method might be overridden, to be notified before the accept method is executed.
	 * <p>
	 * By default, this does nothing.
	 */
	default void beforeExecution() {
	}
}
