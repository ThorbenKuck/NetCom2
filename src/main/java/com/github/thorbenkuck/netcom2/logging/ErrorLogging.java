package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

public class ErrorLogging implements Logging {

	private final Logging style;

	public ErrorLogging() {
		this(new SystemDefaultStyleLogging());
	}

	public ErrorLogging(final Logging base) {
		this.style = base;
	}

	@Override
	public void trace(final String s) {

	}

	@Override
	public void debug(final String s) {

	}

	@Override
	public void info(final String s) {

	}

	@Override
	public void warn(final String s) {

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
