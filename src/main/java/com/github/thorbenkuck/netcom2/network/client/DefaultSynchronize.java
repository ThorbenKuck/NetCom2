package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.AbstractSynchronize;

public class DefaultSynchronize extends AbstractSynchronize {

	public DefaultSynchronize() {
		this(1);
	}

	public DefaultSynchronize(final int numberOfActions) {
		super(numberOfActions);
	}

	@Override
	public void error() {
		Logging.unified().fatal("oh oh..");
	}
}
