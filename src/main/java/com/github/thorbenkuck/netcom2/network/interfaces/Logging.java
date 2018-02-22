package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.logging.*;

public interface Logging {

	/*
	 * Those classes are used, so that only one instance of those Logging instance
	 * are to be kept.
	 *
	 * Those may be accessed to directly via Logging.unified.
	 * Directly accessing those is not recommended! This behaviour might change in the future
	 */
	Logging unified = new NetComLogging();
	Logging disabled = new DisabledLogging();
	Logging callerTrace = new CallerReflectionLogging();
	Logging trace = new TraceLogging();
	Logging debug = new DebugLogging();
	Logging info = new InfoLogging();
	Logging warn = new WarnLogging();
	Logging error = new ErrorLogging();

	static Logging getDefault() {
		return error();
	}

	static Logging disabled() {
		return disabled;
	}

	static Logging unified() {
		return unified;
	}

	static Logging callerTrace() {
		return callerTrace;
	}

	static Logging trace() {
		return trace;
	}

	static Logging debug() {
		return debug;
	}

	static Logging info() {
		return info;
	}

	static Logging warn() {
		return warn;
	}

	static Logging error() {
		return error;
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
	 *
	 * May be caused by a Throwable
	 *
	 * @param s the String that should be logged.
	 */
	void error(final String s, final Throwable throwable);

	/**
	 * Prints something, that failed but is recoverable.
	 *
	 * May be caused by a Throwable.
	 *
	 * Inverse to {@link #error(String, Throwable)}
	 *
	 * @param s the String that should be logged.
	 */
	default void error(final Throwable throwable, final String s) {
		error(s, throwable);
	}

	/**
	 * Prints something, that failed and is not recoverable.
	 *
	 * @param s the String that should be logged.
	 */
	void fatal(final String s);

	/**
	 * Prints something, that failed and is not recoverable.
	 *
	 * May be caused by a Throwable.
	 *
	 * @param s the String that should be logged.
	 */
	void fatal(final String s, final Throwable throwable);

	/**
	 * Prints something, that failed and is not recoverable.
	 *
	 * May be caused by a Throwable.
	 *
	 * Inverse to {@link #fatal(String, Throwable)}
	 *
	 * @param s the String that should be logged.
	 */
	default void fatal(final Throwable throwable, String s) {
		fatal(s, throwable);
	}

	/**
	 * Prints a Throwable, that was encountered some how.
	 *
	 * @param throwable the Throwable, that should be logged.
	 */
	void catching(final Throwable throwable);
}
