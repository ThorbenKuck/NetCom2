package com.github.thorbenkuck.netcom2.network.synchronization;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.AbstractSynchronize;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This is the default entry for Synchronize-instances.
 *
 * You may provide an Runnable, that should be run if an error occurs
 *
 * @version 1.0
 * @since 1.0
 */
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
		NetCom2Utils.parameterNotNull(runnable);
		synchronized (this) {
			onError = runnable;
		}
	}
}
