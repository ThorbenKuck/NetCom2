package de.thorbenkuck.netcom2.network.interfaces;

import de.thorbenkuck.netcom2.logging.*;

public interface Logging {
	static Logging getDefault() {
		return new SystemLogging();
	}

	static Logging disabled() {
		return new DisabledLogging();
	}

	static Logging unified() {
		return new NetComLogging();
	}

	static Logging debug() {
		return new DebugLogging();
	}

	static Logging callerTrace() {
		return new CallerReflectionLoggin();
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
