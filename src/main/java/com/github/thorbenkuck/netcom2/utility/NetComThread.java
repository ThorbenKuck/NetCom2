package com.github.thorbenkuck.netcom2.utility;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

/**
 * NetCom2 uses its own Threads such that developers can easily distinguish between NetCom2 Threads
 * and application Threads.
 *
 * @version 1.0
 * @since 1.0
 */
public class NetComThread extends Thread {


	private NetComThreadContainer netComThreadContainer;

	/**
	 * {@inheritDoc}
	 */
	public NetComThread() {
		setup();
	}

	/**
	 * {@inheritDoc}
	 */
	public NetComThread(Runnable runnable) {
		super(runnable);
		setup();
	}

	/**
	 * {@inheritDoc}
	 */
	public NetComThread(ThreadGroup group, Runnable target) {
		super(group, target);
		setup();
	}

	/**
	 * {@inheritDoc}
	 */
	public NetComThread(String name) {
		super(name);
		setup();
	}

	/**
	 * {@inheritDoc}
	 */
	public NetComThread(ThreadGroup group, String name) {
		super(group, name);
		setup();
	}

	/**
	 * {@inheritDoc}
	 */
	public NetComThread(Runnable target, String name) {
		super(target, name);
		setup();
	}

	/**
	 * {@inheritDoc}
	 */
	public NetComThread(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
		setup();
	}

	/**
	 * Makes this Thread
	 * <ul>
	 * <li><b>not</b> daemon</li>
	 * <li>priority 7</li>
	 * <li>named like a NetCom2 Thread</li>
	 * <li>log uncaught exceptions</li>
	 * </ul>
	 * The priority is such that it is higher than normal, but still not highest
	 */
	private void setup() {
		setDaemon(false);
		setPriority(7);
		setName(NetComThreadFactory.NET_COM_THREAD_NAME);
		setUncaughtExceptionHandler((encounteredThread, throwable) -> Logging.unified().error("Unhandled Exception on NetComThread!", throwable));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		super.run();
		finished();
	}


	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		if (!getName().equals(getThreadGroup().getName())) {
			return getName() + "[" + getThreadGroup().getName() + "#" + getId() + "]";
		} else {
			return getName();
		}
	}

	/**
	 * Removed this thread from its thread container, if it isn't null.
	 */
	public void finished() {
		synchronized (this) {
			if (netComThreadContainer != null) {
				netComThreadContainer.removeThread(this);
			}
		}
	}

	/**
	 * Gets the internal thread container
	 *
	 * @return The thread container
	 */
	public NetComThreadContainer getNetComThreadContainer() {
		synchronized (this) {
			return netComThreadContainer;
		}
	}

	/**
	 * Sets the internal thread container to the specified one.
	 *
	 * @param netComThreadContainer The new thread container
	 */
	void setNetComThreadContainer(NetComThreadContainer netComThreadContainer) {
		NetCom2Utils.parameterNotNull(netComThreadContainer);
		synchronized (this) {
			this.netComThreadContainer = netComThreadContainer;
		}
	}
}
