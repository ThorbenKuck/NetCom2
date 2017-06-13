package de.thorbenkuck.netcom2.logging;

import de.thorbenkuck.netcom2.network.interfaces.Logging;

public class DebugLogging implements Logging {

	private Logging systemLogging = new SystemLogging();

	@Override
	public void trace(String s) {
	}

	@Override
	public void debug(String s) {
		systemLogging.debug(s);
	}

	@Override
	public void info(String s) {
		systemLogging.info(s);
	}

	@Override
	public void warn(String s) {
		systemLogging.warn(s);
	}

	@Override
	public void error(String s) {
		systemLogging.error(s);
	}

	@Override
	public void error(String s, Throwable throwable) {
		systemLogging.error(s, throwable);
	}

	@Override
	public void fatal(String s) {
		systemLogging.fatal(s);
	}

	@Override
	public void fatal(String s, Throwable throwable) {
		systemLogging.fatal(s, throwable);
	}

	@Override
	public void catching(Throwable throwable) {
		systemLogging.catching(throwable);
	}
}
