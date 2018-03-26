package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.util.concurrent.CountDownLatch;

/**
 * This Class helps with creating a Synchronize instance, based on an {@link CountDownLatch}
 *
 * The only method, you would have to define is: {@link Synchronize#error()}
 *
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractSynchronize implements Synchronize {

	protected final int numberOfActions;
	protected final Logging logging = Logging.unified();
	protected CountDownLatch countDownLatch;

	protected AbstractSynchronize() {
		this(1);
	}

	protected AbstractSynchronize(final int numberOfActions) {
		if (numberOfActions < 1) {
			throw new IllegalArgumentException("Number of actions cannot be smaller than 1!");
		}
		this.numberOfActions = numberOfActions;
		this.countDownLatch = new CountDownLatch(numberOfActions);
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public void synchronize() throws InterruptedException {
		logging.trace("Awaiting Synchronization ..");
		countDownLatch.await();
		logging.trace("Synchronized!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public void goOn() {
		logging.debug("Continuing " + this);
		synchronized (this) {
			countDownLatch.countDown();
			logging.debug("Leftover actions to take: " + countDownLatch.getCount());
		}
	}

	/**
	 * {@inheritDoc}
	 */
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
