package com.github.thorbenkuck.netcom2.logging;

public interface Logging {

	/**
	 * Creates the default logging-level.
	 *
	 * @return an Logging instance
	 */
	static Logging getDefault() {
		return warn();
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
	void trace(final Object s);

	/**
	 * Prints something at the second lowest logging depth.
	 *
	 * @param s the String that should be logged.
	 */
	void debug(final Object s);

	/**
	 * Prints something at the default logging depth.
	 *
	 * @param s the String that should be logged.
	 */
	void info(final Object s);

	/**
	 * Prints something, that might produce errors.
	 *
	 * @param s the String that should be logged.
	 */
	void warn(final Object s);

	/**
	 * Prints something, that might produce errors.
	 *
	 * @param o the Object that should be logged.
	 */
	default void warn(final Object o, Throwable throwable) {
		warn(o);
		catching(throwable);
	}

	/**
	 * Prints something, that failed but is recoverable.
	 *
	 * @param s the String that should be logged.
	 */
	void error(final Object s);

	/**
	 * Prints something, that failed but is recoverable.
	 * <p>
	 * May be caused by a Throwable
	 *
	 * @param s the String that should be logged.
	 */
	default void error(final Object s, final Throwable throwable) {
		error(s);
		catching(throwable);
	}

	/**
	 * Prints something, that failed and is not recoverable.
	 *
	 * @param s the String that should be logged.
	 */
	void fatal(final Object s);

	/**
	 * Prints something, that failed and is not recoverable.
	 * <p>
	 * May be caused by a Throwable.
	 *
	 * @param s the String that should be logged.
	 */
	default void fatal(final Object s, final Throwable throwable) {
		fatal(s);
		catching(throwable);
	}

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
	 * Inverse to {@link #error(Object, Throwable)}
	 *
	 * @param s the String that should be logged.
	 */
	default void error(final Throwable throwable, final Object s) {
		error(s, throwable);
	}

	/**
	 * Prints something, that failed and is not recoverable.
	 * <p>
	 * May be caused by a Throwable.
	 * <p>
	 * Inverse to {@link #fatal(Object, Throwable)}
	 *
	 * @param s the String that should be logged.
	 */
	default void fatal(final Throwable throwable, Object s) {
		fatal(s, throwable);
	}

	default void instantiated(Object object) {
		Class<?> type = object.getClass();
		if(type.getInterfaces().length == 1) {
			instantiated(type.getInterfaces()[0].getSimpleName() + " as " + type.getSimpleName());
		} else {
			instantiated(type.getSimpleName());
		}
	}

	default void instantiated(String objectName) {
		debug("Instantiated " + objectName);
	}
}
