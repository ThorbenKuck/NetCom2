package de.thorbenkuck.netcom2.logging;

import de.thorbenkuck.netcom2.annotations.Synchronized;
import de.thorbenkuck.netcom2.network.interfaces.Logging;

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

	public SystemDefaultStyleLogging(PrintStream printStream) {
		out = printStream;
	}

	@Override
	public String toString() {
		return "{Default Logging-style for NetCom2Logging}";
	}

	@Override
	public void trace(String s) {
		println(getPrefix() + "TRACE : " + s);
	}

	@Override
	public void debug(String s) {
		println(getPrefix() + "DEBUG : " + s);
	}

	@Override
	public void info(String s) {
		println(getPrefix() + "INFO : " + s);
	}

	@Override
	public void warn(String s) {
		println(getPrefix() + "WARN : " + s);
	}

	@Override
	public void error(String s) {
		println(getPrefix() + "ERROR : " + s);
	}

	@Override
	public void error(String s, Throwable throwable) {
		error(s);
		catching(throwable);
	}

	@Override
	public void fatal(String s) {
		println(getPrefix() + "FATAL : " + s);
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
		println(stacktrace);
	}

	private void println(String s) {
		synchronized (out) {
			out.println(s);
		}
	}

	String getPrefix() {
		return "[" + LocalDateTime.now() + "] (" + Thread.currentThread().toString() + ") ";
	}


}
