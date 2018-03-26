package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

/**
 * This interface has only default methods.
 * <p>
 * You may override any number of those, but you are not required to.
 * <p>
 * The DefaultFamily currently includes:
 * <p>
 * <ul>
 * <li>{@link OnReceiveSingle}</li>
 * <li>{@link OnReceive}</li>
 * <li>{@link OnReceiveTriple}</li>
 * </ul>
 *
 * @version 1.0
 * @since 1.0
 */
public interface ReceiveFamily {

	/**
	 * If either {@link #beforeExecution()}, {@link OnReceive#accept(Object, Object)} or {@link #successfullyExecuted()}
	 * throws an exception, the execution is stopped and this method is called.
	 * <p>
	 * This means, if <code>beforeExecution</code> throws an Exception, <code>accept</code> is never called!
	 *
	 * @param e the encountered Exception
	 */
	default void exceptionEncountered(Exception e) {
		Logging.unified().error(this + " encountered an Exception!", e);
	}

	/**
	 * This method might be overridden, to be notified if this implementation was successfully executed
	 * <p>
	 * By default, this does nothing
	 */
	default void successfullyExecuted() {
	}

	/**
	 * This method might be overridden, to be notified before this Interface is executed
	 * <p>
	 * By default, this does nothing
	 */
	default void beforeExecution() {
	}
}
