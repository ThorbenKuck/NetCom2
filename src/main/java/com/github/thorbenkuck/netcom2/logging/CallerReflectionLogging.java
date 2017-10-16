package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.util.Objects;

public class CallerReflectionLogging implements Logging {

	private Logging logging;

	public CallerReflectionLogging() {
		this(new CallerTraceSystemDefaultStyleLogging());
	}

	public CallerReflectionLogging(Logging base) {
		Objects.requireNonNull(base);
		this.logging = base;
		warn("This Logging-Mechanism is very workload-intensive!");
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
		error(s);
		catching(throwable);
	}

	@Override
	public void fatal(String s) {
		logging.fatal(s);
	}

	@Override
	public void fatal(String s, Throwable throwable) {
		fatal(s);
		catching(throwable);
	}

	@Override
	public void catching(Throwable throwable) {
		logging.catching(throwable);
	}

}
