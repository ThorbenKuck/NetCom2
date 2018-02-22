package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

/**
 * This interface has only default methods.
 * You may override any number of those, but you are not required to.
 */
public interface ReceiveFamily {

	/**
	 * If either {@link #beforeExecution()}, {@link OnReceive#accept(Object, Object)} or {@link #successfullyExecuted()}
	 * throws an exception, the execution is stopped and this method is called.
	 *
	 * This means, if <code>beforeExecution</code> throws an Exception, <code>accept</code> is never called!
	 *
	 * @param e the encountered Exception
	 */
	default void exceptionEncountered(Exception e) {
		Logging.unified().error(this + " encountered an Exception!", e);
	}

	default void successfullyExecuted() {
	}

	default void beforeExecution() {
	}
}
