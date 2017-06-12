package de.thorbenkuck.netcom2.network.interfaces;

import de.thorbenkuck.netcom2.logging.DebugLogging;
import de.thorbenkuck.netcom2.logging.DisabledLogging;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.logging.SystemLogging;

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

	void trace(String s);

	void info(String s);

	void debug(String s);

	void warn(String s);

	void error(String s);

	void error(String s, Throwable throwable);

	void fatal(String s);

	void fatal(String s, Throwable throwable);

	void catching(Throwable throwable);
}
