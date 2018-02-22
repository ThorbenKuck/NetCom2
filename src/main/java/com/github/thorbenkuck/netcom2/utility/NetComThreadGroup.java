package com.github.thorbenkuck.netcom2.utility;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

public class NetComThreadGroup extends ThreadGroup {

	private final Logging logging = Logging.unified();

	public NetComThreadGroup(String name) {
		super(name);
	}

	public NetComThreadGroup(ThreadGroup parent, String name) {
		super(parent, name);
	}
}
