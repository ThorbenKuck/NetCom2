package com.github.thorbenkuck.netcom2.utility;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

/**
 * Represents a NetCom2 thread groups.
 * <p>
 * It is currently debated if this class is really needed.
 *
 * @version 1.0
 * @since 1.0
 */
public class NetComThreadGroup extends ThreadGroup {

	private final Logging logging = Logging.unified();

	public NetComThreadGroup(String name) {
		super(name);
	}

	public NetComThreadGroup(ThreadGroup parent, String name) {
		super(parent, name);
	}
}
