package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.AbstractSynchronize;

public class DefaultSynchronize extends AbstractSynchronize {

	private Runnable onError = () -> Logging.unified().fatal(this + ": error notification received!");

	public DefaultSynchronize() {
		this(1);
	}

	public DefaultSynchronize(final int numberOfActions) {
		super(numberOfActions);
	}

	/**
	 * This Method will call the Runnable, set via {@link #setOnError(Runnable)}.
	 * {@inheritDoc}
	 */
	@Override
	public void error() {
		synchronized (this) {
			onError.run();
		}
	}

	/**
	 * Sets an Runnable, that should be executed if an error occurred
	 *
	 * @param runnable the runnable, that should be executed.
	 */
	public void setOnError(Runnable runnable) {
		synchronized (this) {
			onError = runnable;
		}
	}
}
