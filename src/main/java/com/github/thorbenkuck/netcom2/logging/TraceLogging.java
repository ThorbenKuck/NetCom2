package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

public class TraceLogging implements Logging {

	private final Logging logging;

	public TraceLogging() {
		this(new SystemDefaultStyleLogging());
	}

	public TraceLogging(Logging logging) {
		this.logging = logging;
	}

	@Override
	public void trace(String s) {
		logging.trace(s);
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
