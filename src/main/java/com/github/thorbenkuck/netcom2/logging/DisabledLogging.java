package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

public class DisabledLogging implements Logging {
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
	}

	@Override
	public void error(final String s, final Throwable throwable) {
	}

	@Override
	public void fatal(final String s) {
	}

	@Override
	public void fatal(final String s, final Throwable throwable) {
	}

	@Override
	public void catching(final Throwable throwable) {
	}
}
