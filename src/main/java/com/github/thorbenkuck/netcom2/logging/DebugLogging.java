package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

public class DebugLogging implements Logging {

	private final Logging logging;

	public DebugLogging() {
		this(new SystemDefaultStyleLogging());
	}

	public DebugLogging(Logging base) {
		this.logging = base;
	}

	@Override
	public void trace(String s) {
	}

	@Override
	public void debug(String s) {
		logging.debug(s);
	}

	@Override
	public void info(String s) {
		logging.info(s);
	}

	@Override
	public void warn(String s) {
		logging.warn(s);
	}

	@Override
	public void error(String s) {
		logging.error(s);
	}

	@Override
	public void error(String s, Throwable throwable) {
		logging.error(s, throwable);
	}

	@Override
	public void fatal(String s) {
		logging.fatal(s);
	}

	@Override
	public void fatal(String s, Throwable throwable) {
		logging.fatal(s, throwable);
	}

	@Override
	public void catching(Throwable throwable) {
		logging.catching(throwable);
	}
}
