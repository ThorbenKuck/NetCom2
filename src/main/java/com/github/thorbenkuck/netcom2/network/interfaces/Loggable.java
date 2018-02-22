package com.github.thorbenkuck.netcom2.network.interfaces;

public interface Loggable {

	/**
	 * Allows to override internally set Logging-instances.
	 *
	 * By default, every component uses the {@link Logging#unified()}, therefor, by calling:
	 *
	 * <code>
	 *     Logging instance = ...
	 *     NetComLogging.setLogging(instance);
	 * </code>
	 *
	 * you will update the internally used logging mechanisms of all components at the same time.
	 *
	 * @param logging the Logging instance that should be used.
	 */
	void setLogging(final Logging logging);
}
