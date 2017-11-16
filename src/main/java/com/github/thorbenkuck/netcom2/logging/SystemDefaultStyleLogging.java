package com.github.thorbenkuck.netcom2.logging;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Synchronized
public class SystemDefaultStyleLogging implements Logging {

	private final PrintStream out;

	public SystemDefaultStyleLogging() {
		this(System.out);
	}

	public SystemDefaultStyleLogging(final PrintStream printStream) {
		out = printStream;
	}

	@Override
	public String toString() {
		return "{Default Logging-style for NetCom2Logging}";
	}

	@Override
	public void trace(final String s) {
		println(getPrefix() + "TRACE : " + s);
	}

	@Override
	public void debug(final String s) {
		println(getPrefix() + "DEBUG : " + s);
	}

	@Override
	public void info(final String s) {
		println(getPrefix() + "INFO : " + s);
	}

	@Override
	public void warn(final String s) {
		println(getPrefix() + "WARN : " + s);
	}

	@Override
	public void error(final String s) {
		println(getPrefix() + "ERROR : " + s);
	}

	@Override
	public void error(final String s, final Throwable throwable) {
		error(s);
		catching(throwable);
	}

	@Override
	public void fatal(final String s) {
		println(getPrefix() + "FATAL : " + s);
	}

	@Override
	public void fatal(final String s, final Throwable throwable) {
		fatal(s);
		catching(throwable);
	}

	@Override
	public void catching(final Throwable throwable) {
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		println(stacktrace);
	}

	private void println(final String s) {
		synchronized (out) {
			out.println(s);
		}
	}

	String getPrefix() {
		return "[" + LocalDateTime.now() + "] (" + Thread.currentThread().toString() + ") ";
	}
}
