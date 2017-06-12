package de.thorbenkuck.netcom2.logging;

import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class SystemLogging implements Logging {
	@Override
	public String toString() {
		return "{Default Logging-style for NetCom2-LoggingUtil}";
	}

	@Override
	public void trace(String s) {
		System.out.println("[" + new Date().toString() + "] (" + Thread.currentThread().toString() + ") TRACE : " + s);
	}

	@Override
	public void debug(String s) {
		System.out.println("[" + new Date().toString() + "] (" + Thread.currentThread().toString() + ") DEBUG : " + s);
	}

	@Override
	public void info(String s) {
		System.out.println("[" + new Date().toString() + "] (" + Thread.currentThread().toString() + ") INFO : " + s);
	}

	@Override
	public void warn(String s) {
		System.out.println("[" + new Date().toString() + "] (" + Thread.currentThread().toString() + ") WARN : " + s);
	}

	@Override
	public void error(String s) {
		System.out.println("[" + new Date().toString() + "] (" + Thread.currentThread().toString() + ") ERROR : " + s);
	}

	@Override
	public void error(String s, Throwable throwable) {
		error(s);
		catching(throwable);
	}

	@Override
	public void fatal(String s) {
		System.out.println("[" + new Date().toString() + "] (" + Thread.currentThread().toString() + ") FATAL : " + s);
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
		System.out.println(stacktrace);
	}


}
