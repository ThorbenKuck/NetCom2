package com.github.thorbenkuck.netcom2.logging;

/**
 * This class is inherited from the {@link SystemDefaultStyleLogging} and overrides the {@link #getPrefix()} method to
 * inject the the via reflection determined caller of any method.
 */
class CallerTraceSystemDefaultStyleLogging extends SystemDefaultStyleLogging {

	/**
	 * {@inheritDoc}
	 *
	 * @return the super.getPrefix combined with the {@link #getCaller()}
	 */
	@Override
	public String getPrefix() {
		return super.getPrefix() + "[" + getCaller() + "] ";
	}

	/**
	 * Determines the caller of any method by analysing the stackTrace of the current Thread.
	 * <p>
	 * This is very workload intensive and should be used with care!
	 *
	 * @return the class name of the class, calling any method to log something or null if no valid name is found
	 */
	String getCaller() {
		final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
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
}
