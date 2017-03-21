package de.thorbenkuck.netcom2.logging;

import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.util.Date;

public class SysoLogging implements Logging {
	@Override
	public void catching(Throwable throwable) {
		throwable.printStackTrace();
	}

	@Override
	public void debug(String s) {
		System.out.println("[" + new Date().toString() + "] DEBUG : " + s);
	}

	@Override
	public void info(String s) {
		System.out.println("[" + new Date().toString() + "] INFO : " + s);
	}

	@Override
	public void trace(String s) {
		System.out.println("[" + new Date().toString() + "] TRACE : " + s);
	}

	@Override
	public void warn(String s) {
		System.out.println("[" + new Date().toString() + "] WARN : " + s);
	}

	@Override
	public void error(String s) {
		System.out.println("[" + new Date().toString() + "] ERROR : " + s);
	}

	@Override
	public void error(String s, Throwable throwable) {
		error(s);
		catching(throwable);
	}

	@Override
	public void fatal(String s) {
		System.out.println("[" + new Date().toString() + "] FATAL : " + s);
	}

	@Override
	public void fatal(String s, Throwable throwable) {
		fatal(s);
		catching(throwable);
	}
}
