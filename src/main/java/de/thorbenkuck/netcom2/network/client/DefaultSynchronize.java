package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.AbstractSynchronize;

public class DefaultSynchronize extends AbstractSynchronize {

	public DefaultSynchronize() {
		this(1);
	}

	public DefaultSynchronize(int numberOfActions) {
		super(numberOfActions);
	}

	@Override
	public void error() {
		Logging.unified().fatal("oh oh..");
	}
}
