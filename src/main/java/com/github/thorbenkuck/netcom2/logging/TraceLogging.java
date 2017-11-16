package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

public class TraceLogging implements Logging {

	private final Logging style;

	public TraceLogging() {
		this(new SystemDefaultStyleLogging());
	}

	public TraceLogging(final Logging base) {
		this.style = base;
	}

	@Override
	public void trace(final String s) {
		style.trace(s);
	}

	@Override
	public void debug(final String s) {
		style.debug(s);
	}

	@Override
	public void info(final String s) {
		style.info(s);
	}

	@Override
	public void warn(final String s) {
		style.warn(s);
	}

	@Override
	public void error(final String s) {
		style.error(s);
	}

	@Override
	public void error(final String s, final Throwable throwable) {
		style.error(s, throwable);
	}

	@Override
	public void fatal(final String s) {
		style.fatal(s);
	}

	@Override
	public void fatal(final String s, final Throwable throwable) {
		style.fatal(s, throwable);
	}

	@Override
	public void catching(final Throwable throwable) {
		style.catching(throwable);
	}
}
