package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.logging.NetComLogging;

/**
 * This interface is the core interface as entry for the Logging api.
 *
 * @version 1.0
 * @since 1.0
 * @deprecated This class is relocated to {@link com.github.thorbenkuck.netcom2.logging}. This makes more sens
 */
@Deprecated
public interface Logging {

	/**
	 * Creates the default logging-level.
	 *
	 * @return an Logging instance
	 */
	static com.github.thorbenkuck.netcom2.logging.Logging getDefault() {
		return error();
	}

	/**
	 * Creates a logging-level, that does not log anything
	 *
	 * @return an Logging instance
	 */
	static com.github.thorbenkuck.netcom2.logging.Logging disabled() {
		return com.github.thorbenkuck.netcom2.logging.Logging.disabled();
	}

	/**
	 * Creates logging instance, that updates with the {@link NetComLogging#setLogging(com.github.thorbenkuck.netcom2.logging.Logging)} instance
	 *
	 * @return an Logging instance
	 */
	static com.github.thorbenkuck.netcom2.logging.Logging unified() {
		return com.github.thorbenkuck.netcom2.logging.Logging.unified();
	}

	/**
	 * Creates a logging-level of trace, but analyses the Stacktrace to find the caller of that method.
	 *
	 * @return an Logging instance
	 */
	static com.github.thorbenkuck.netcom2.logging.Logging callerTrace() {
		return com.github.thorbenkuck.netcom2.logging.Logging.callerTrace();
	}

	/**
	 * Creates the trace logging-level (logs everything).
	 *
	 * @return an Logging instance
	 */
	static com.github.thorbenkuck.netcom2.logging.Logging trace() {
		return com.github.thorbenkuck.netcom2.logging.Logging.trace();
	}

	/**
	 * Creates the debug logging-level (does not log trace calls).
	 *
	 * @return an Logging instance
	 */
	static com.github.thorbenkuck.netcom2.logging.Logging debug() {
		return com.github.thorbenkuck.netcom2.logging.Logging.debug();
	}

	/**
	 * Creates the info logging-level (does not log trace and debug calls).
	 *
	 * @return an Logging instance
	 */
	static com.github.thorbenkuck.netcom2.logging.Logging info() {
		return com.github.thorbenkuck.netcom2.logging.Logging.info();
	}

	/**
	 * Creates the debug logging-level (does not log trace, debug and info calls).
	 *
	 * @return an Logging instance
	 */
	static com.github.thorbenkuck.netcom2.logging.Logging warn() {
		return com.github.thorbenkuck.netcom2.logging.Logging.warn();
	}

	/**
	 * Creates the debug logging-level (does not log trace, debug, info and warn calls).
	 *
	 * @return an Logging instance
	 */
	static com.github.thorbenkuck.netcom2.logging.Logging error() {
		return com.github.thorbenkuck.netcom2.logging.Logging.error();
	}

	/**
	 * Prints something at the lowest logging depth.
	 *
	 * @param s the String that should be logged.
	 */
	void trace(final String s);

	/**
	 * Prints something at the second lowest logging depth.
	 *
	 * @param s the String that should be logged.
	 */
	void debug(final String s);

	/**
	 * Prints something at the default logging depth.
	 *
	 * @param s the String that should be logged.
	 */
	void info(final String s);

	/**
	 * Prints something, that might produce errors.
	 *
	 * @param s the String that should be logged.
	 */
	void warn(final String s);

	/**
	 * Prints something, that failed but is recoverable.
	 *
	 * @param s the String that should be logged.
	 */
	void error(final String s);

	/**
	 * Prints something, that failed but is recoverable.
	 * <p>
	 * May be caused by a Throwable
	 *
	 * @param s the String that should be logged.
	 */
	void error(final String s, final Throwable throwable);

	/**
	 * Prints something, that failed and is not recoverable.
	 *
	 * @param s the String that should be logged.
	 */
	void fatal(final String s);

	/**
	 * Prints something, that failed and is not recoverable.
	 * <p>
	 * May be caused by a Throwable.
	 *
	 * @param s the String that should be logged.
	 */
	void fatal(final String s, final Throwable throwable);

	/**
	 * Prints a Throwable, that was encountered some how.
	 *
	 * @param throwable the Throwable, that should be logged.
	 */
	void catching(final Throwable throwable);

	/**
	 * Prints something, that failed but is recoverable.
	 * <p>
	 * May be caused by a Throwable.
	 * <p>
	 * Inverse to {@link #error(String, Throwable)}
	 *
	 * @param s the String that should be logged.
	 */
	default void error(final Throwable throwable, final String s) {
		error(s, throwable);
	}

	/**
	 * Prints something, that failed and is not recoverable.
	 * <p>
	 * May be caused by a Throwable.
	 * <p>
	 * Inverse to {@link #fatal(String, Throwable)}
	 *
	 * @param s the String that should be logged.
	 */
	default void fatal(final Throwable throwable, String s) {
		fatal(s, throwable);
	}
}
