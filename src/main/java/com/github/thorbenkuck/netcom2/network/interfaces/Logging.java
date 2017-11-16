package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.logging.*;

public interface Logging {
	static Logging getDefault() {
		return error();
	}

	static Logging disabled() {
		return new DisabledLogging();
	}

	static Logging unified() {
		return new NetComLogging();
	}

	static Logging callerTrace() {
		return new CallerReflectionLogging();
	}

	static Logging trace() {
		return new TraceLogging();
	}

	static Logging debug() {
		return new DebugLogging();
	}

	static Logging info() {
		return new InfoLogging();
	}

	static Logging warn() {
		return new WarnLogging();
	}

	static Logging error() {
		return new ErrorLogging();
	}

	void trace(final String s);

	void debug(final String s);

	void info(final String s);

	void warn(final String s);

	void error(final String s);

	void error(final String s, final Throwable throwable);

	void fatal(final String s);

	void fatal(final String s, final Throwable throwable);

	void catching(final Throwable throwable);
}
