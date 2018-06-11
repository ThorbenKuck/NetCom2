package com.github.thorbenkuck.netcom2.logging;

public interface Logging {

	/**
	 * Creates the default logging-level.
	 *
	 * @return an Logging instance
	 */
	static Logging getDefault() {
		return error();
	}

	/**
	 * Creates a logging-level, that does not log anything
	 *
	 * @return an Logging instance
	 */
	static Logging disabled() {
		return new DisabledLogging();
	}

	/**
	 * Creates logging instance, that updates with the {@link NetComLogging#setLogging(Logging)} instance
	 *
	 * @return an Logging instance
	 */
	static Logging unified() {
		return new NetComLogging();
	}

	/**
	 * Creates a logging-level of trace, but analyses the Stacktrace to find the caller of that method.
	 *
	 * @return an Logging instance
	 */
	static Logging callerTrace() {
		return new CallerReflectionLogging();
	}

	/**
	 * Creates the trace logging-level (logs everything).
	 *
	 * @return an Logging instance
	 */
	static Logging trace() {
		return new TraceLogging();
	}

	/**
	 * Creates the debug logging-level (does not log trace calls).
	 *
	 * @return an Logging instance
	 */
	static Logging debug() {
		return new DebugLogging();
	}

	/**
	 * Creates the info logging-level (does not log trace and debug calls).
	 *
	 * @return an Logging instance
	 */
	static Logging info() {
		return new InfoLogging();
	}

	/**
	 * Creates the debug logging-level (does not log trace, debug and info calls).
	 *
	 * @return an Logging instance
	 */
	static Logging warn() {
		return new WarnLogging();
	}

	/**
	 * Creates the debug logging-level (does not log trace, debug, info and warn calls).
	 *
	 * @return an Logging instance
	 */
	static Logging error() {
		return new ErrorLogging();
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
