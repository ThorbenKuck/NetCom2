package com.github.thorbenkuck.netcom2.utility;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

public class NetComThread extends Thread {

	private NetComThreadContainer netComThreadContainer;

	public NetComThread() {
		setup();
	}

	public NetComThread(Runnable runnable) {
		super(runnable);
		setup();
	}

	public NetComThread(ThreadGroup group, Runnable target) {
		super(group, target);
		setup();
	}

	public NetComThread(String name) {
		super(name);
		setup();
	}

	public NetComThread(ThreadGroup group, String name) {
		super(group, name);
		setup();
	}

	public NetComThread(Runnable target, String name) {
		super(target, name);
		setup();
	}

	public NetComThread(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
		setup();
	}

	private void setup() {
		setDaemon(false);
		setPriority(7);
		setName(NetComThreadFactory.NET_COM_THREAD_NAME);
		setUncaughtExceptionHandler((encounteredThread, throwable) -> Logging.unified().error("Unhandled Exception on NetComThread!", throwable));
	}

	@Override
	public void run() {
		super.run();
		finished();
	}

	public String toString() {
		if (! getName().equals(getThreadGroup().getName())) {
			return getName() + "[" + getThreadGroup().getName() + "#" + getId() + "]";
		} else {
			return getName();
		}
	}

	public void finished() {
		synchronized (this) {
			if (netComThreadContainer != null) {
				netComThreadContainer.removeThread(this);
			}
		}
	}

	public NetComThreadContainer getNetComThreadContainer() {
		synchronized (this) {
			return netComThreadContainer;
		}
	}

	void setNetComThreadContainer(NetComThreadContainer netComThreadContainer) {
		NetCom2Utils.parameterNotNull(netComThreadContainer);
		synchronized (this) {
			this.netComThreadContainer = netComThreadContainer;
		}
	}
}
