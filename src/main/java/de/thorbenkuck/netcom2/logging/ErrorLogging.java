package de.thorbenkuck.netcom2.logging;

import de.thorbenkuck.netcom2.network.interfaces.Logging;

public class ErrorLogging implements Logging {

	private final Logging logging;

	public ErrorLogging() {
		this(new SystemDefaultStyleLogging());
	}

	public ErrorLogging(Logging base) {
		this.logging = base;
	}

	@Override
	public void trace(String s) {

	}

	@Override
	public void debug(String s) {

	}

	@Override
	public void info(String s) {

	}

	@Override
	public void warn(String s) {

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
