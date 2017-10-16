package com.github.thorbenkuck.netcom2.logging;

class CallerTraceSystemDefaultStyleLogging extends SystemDefaultStyleLogging {

	public String getCaller() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for (StackTraceElement stackTraceElement : stackTraceElements) {
			if (! stackTraceElement.getClassName().equals(CallerReflectionLogging.class.getName())
					&& ! stackTraceElement.getClassName().equals(NetComLogging.class.getName())
					&& ! stackTraceElement.getClassName().equals(CallerTraceSystemDefaultStyleLogging.class.getName())
					&& ! stackTraceElement.getClassName().equals(SystemDefaultStyleLogging.class.getName())
					&& stackTraceElement.getClassName().indexOf("java.lang.Thread") != 0) {
				return stackTraceElement.getClassName();
			}
		}
		return null;
	}

	@Override
	public String getPrefix() {
		return super.getPrefix() + "[" + getCaller() + "] ";
	}
}
