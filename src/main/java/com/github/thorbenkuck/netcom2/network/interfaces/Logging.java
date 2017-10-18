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

	void trace(String s);

	void debug(String s);

	void info(String s);

	void warn(String s);

	void error(String s);

	void error(String s, Throwable throwable);

	void fatal(String s);

	void fatal(String s, Throwable throwable);

	void catching(Throwable throwable);
}
