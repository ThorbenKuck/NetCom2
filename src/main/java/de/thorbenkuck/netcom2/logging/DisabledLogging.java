package de.thorbenkuck.netcom2.logging;

import de.thorbenkuck.netcom2.network.interfaces.Logging;

public class DisabledLogging implements Logging {
	@Override
	public void catching(Throwable throwable) {

	}

	@Override
	public void debug(String s) {

	}

	@Override
	public void info(String s) {

	}

	@Override
	public void trace(String s) {

	}

	@Override
	public void warn(String s) {

	}

	@Override
	public void error(String s) {

	}

	@Override
	public void error(String s, Throwable throwable) {

	}

	@Override
	public void fatal(String s) {

	}

	@Override
	public void fatal(String s, Throwable throwable) {

	}
}