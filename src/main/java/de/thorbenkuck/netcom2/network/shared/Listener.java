package de.thorbenkuck.netcom2.network.shared;

import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class Listener implements ListenAndExpect {

	private final Logging logging = Logging.unified();
	private Class t;
	private CountDownLatch countDownLatch = new CountDownLatch(1);
	private boolean removable = true;

	public Listener(Class t) {
		Objects.requireNonNull(t);
		this.t = t;
	}

	@Override
	public final void andWaitFor(Class clazz) throws InterruptedException {
		removable = false;
		this.t = clazz;
		await();
	}

	private void await() throws InterruptedException {
		logging.trace("Awaiting receiving of " + t);
		countDownLatch.await();
		logging.trace("Received " + t + "! Continuing ..");
	}

	@Override
	public final void tryAccept(Class clazz) {
		logging.trace("Trying to match " + clazz + " to " + t);
		if (t.equals(clazz)) {
			trigger();
		}
	}

	@Override
	public final boolean isRemovable() {
		return removable;
	}

	@Override
	public final boolean isAcceptable(Object o) {
		return o != null && o.getClass().equals(t);
	}

	protected final void trigger() {
		logging.trace("Match found! Releasing waiting Threads ..");
		countDownLatch.countDown();
		logging.trace("Marked " + this + " as isRemovable");
		removable = true;
	}

	public String toString() {
		return "Listening for Class: " + t;
	}
}
