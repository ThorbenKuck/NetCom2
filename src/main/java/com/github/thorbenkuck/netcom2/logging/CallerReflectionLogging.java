package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.Requirements;

public class CallerReflectionLogging implements Logging {

	private final Logging style;

	public CallerReflectionLogging() {
		this(new CallerTraceSystemDefaultStyleLogging());
	}

	public CallerReflectionLogging(final Logging base) {
		Requirements.assertNotNull(base);
		this.style = base;
		warn("This Logging-Mechanism is very workload-intensive!");
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
		error(s);
		catching(throwable);
	}

	@Override
	public void fatal(final String s) {
		style.fatal(s);
	}

	@Override
	public void fatal(final String s, final Throwable throwable) {
		fatal(s);
		catching(throwable);
	}

	@Override
	public void catching(final Throwable throwable) {
		style.catching(throwable);
	}

}
