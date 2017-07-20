package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.util.concurrent.CountDownLatch;

public abstract class AbstractSynchronize implements Synchronize {

	protected final int numberOfActions;
	protected final Logging logging = Logging.unified();
	protected CountDownLatch countDownLatch;

	protected AbstractSynchronize() {
		this(1);
	}

	protected AbstractSynchronize(int numberOfActions) {
		if (numberOfActions < 1) {
			throw new IllegalArgumentException("Number of actions cannot be smaller than 1!");
		}
		this.numberOfActions = numberOfActions;
		this.countDownLatch = new CountDownLatch(numberOfActions);
	}

	@Asynchronous
	@Override
	public void synchronize() throws InterruptedException {
		logging.trace("Awaiting Synchronization ..");
		countDownLatch.await();
		logging.trace("Synchronized!");
	}

	@Asynchronous
	@Override
	public void goOn() {
		logging.debug("Continuing " + this);
		synchronized (this) {
			countDownLatch.countDown();
			logging.debug("Leftover actions to take: " + countDownLatch.getCount());
		}
	}

	@Asynchronous
	@Override
	public void reset() {
		logging.debug("Resetting " + this);
		synchronized (this) {
			while (countDownLatch.getCount() > 0) {
				goOn();
			}
			countDownLatch = new CountDownLatch(numberOfActions);
		}
	}
}
