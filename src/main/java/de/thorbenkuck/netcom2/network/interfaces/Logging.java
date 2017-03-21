package de.thorbenkuck.netcom2.network.interfaces;

public interface Logging {
	void catching(Throwable throwable);

	void debug(String s);

	void info(String s);

	void trace(String s);

	void warn(String s);

	void error(String s);

	void error(String s, Throwable throwable);

	void fatal(String s);

	void fatal(String s, Throwable throwable);
}
