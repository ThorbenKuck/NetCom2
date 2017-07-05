package de.thorbenkuck.netcom2.logging;

import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

public class SystemDefaultStyleLogging implements Logging {

	private final PrintStream out;

	public SystemDefaultStyleLogging() {
		this(System.out);
	}

	public SystemDefaultStyleLogging(PrintStream printStream) {
		out = printStream;
	}

	@Override
	public String toString() {
		return "{Default Logging-style for NetCom2Logging}";
	}

	@Override
	public void trace(String s) {
		out.println(getPrefix() + "TRACE : " + s);
	}

	String getPrefix() {
		return "[" + LocalDateTime.now() + "] (" + Thread.currentThread().toString() + ") ";
	}

	@Override
	public void debug(String s) {
		out.println(getPrefix() + "DEBUG : " + s);
	}

	@Override
	public void info(String s) {
		out.println(getPrefix() + "INFO : " + s);
	}

	@Override
	public void warn(String s) {
		out.println(getPrefix() + "WARN : " + s);
	}

	@Override
	public void error(String s) {
		out.println(getPrefix() + "ERROR : " + s);
	}

	@Override
	public void error(String s, Throwable throwable) {
		error(s);
		catching(throwable);
	}

	@Override
	public void fatal(String s) {
		out.println(getPrefix() + "FATAL : " + s);
	}

	@Override
	public void fatal(String s, Throwable throwable) {
		fatal(s);
		catching(throwable);
	}

	@Override
	public void catching(Throwable throwable) {
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		out.println(stacktrace);
	}


}
