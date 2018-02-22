package com.github.thorbenkuck.netcom2.utility;

public class NetComThread extends Thread {

	private NetComThreadContainer netComThreadContainer;

	public NetComThread() {
	}

	public NetComThread(Runnable runnable) {
		super(runnable);
	}

	public NetComThread(ThreadGroup group, Runnable target) {
		super(group, target);
	}

	public NetComThread(String name) {
		super(name);
	}

	public NetComThread(ThreadGroup group, String name) {
		super(group, name);
	}

	public NetComThread(Runnable target, String name) {
		super(target, name);
	}

	public NetComThread(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
	}

	@Override
	public void run() {
		super.run();
		finished();
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

	public void setNetComThreadContainer(NetComThreadContainer netComThreadContainer) {
		synchronized (this) {
			this.netComThreadContainer = netComThreadContainer;
		}
	}

	public String toString() {
		if(!getName().equals(getThreadGroup().getName())) {
			return getName() + "[" + getThreadGroup().getName() + "#" + getId() + "]";
		} else {
			return getName();
		}
	}
}
