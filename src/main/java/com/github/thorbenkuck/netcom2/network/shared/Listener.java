package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class Listener implements ListenAndExpect {

	private final Logging logging = Logging.unified();
	private Class t;
	private CountDownLatch countDownLatch = new CountDownLatch(1);
	private boolean removable = true;
	private boolean changed = false;

	public Listener(Class t) {
		NetCom2Utils.assertNotNull(t);
		this.t = t;
	}

	@Override
	public final void andWaitFor(final Class clazz) throws InterruptedException {
		NetCom2Utils.parameterNotNull(clazz);
		removable = false;
		changed = true;
		this.t = clazz;
		await();
	}

	private void await() throws InterruptedException {
		logging.trace("Awaiting receiving of " + t);
		countDownLatch.await();
		logging.trace("Listener for " + t + " finished! Continuing ..");
	}

	@Override
	public final void tryAccept(final Class clazz) {
		logging.trace("Trying to match " + clazz + " to " + t);
		if (isAcceptable(clazz)) {
			trigger();
		}
	}

	@Override
	public final boolean isRemovable() {
		return removable;
	}

	@Override
	public final boolean isAcceptable(final Object o) {
		return o != null && o.getClass().equals(t);
	}

	protected final void trigger() {
		logging.trace("Match found! Releasing waiting Threads ..");
		countDownLatch.countDown();
		logging.trace("Marked " + this + " as isRemovable");
		removable = true;
	}

	public String toString() {
		return (changed ? "!!!!!!!!!" : "") + "Listening for Class: " + t;
	}
}
