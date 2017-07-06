package de.thorbenkuck.netcom2.network.shared;

import de.thorbenkuck.netcom2.annotations.Asynchronous;
import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.util.concurrent.CountDownLatch;

public abstract class AbstractSynchronize implements Synchronize {

	private final int numberOfActions;
	private final Logging logging = Logging.unified();
	private CountDownLatch countDownLatch;

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
	public final void synchronize() throws InterruptedException {
		logging.trace("Awaiting Synchronization ..");
		countDownLatch.await();
		logging.trace("Synchronized!");
	}

	@Asynchronous
	@Override
	public final void goOn() {
		logging.debug("Continuing " + this);
		synchronized (logging) {
			countDownLatch.countDown();
			logging.debug("Leftover actions to take: " + countDownLatch.getCount());
		}
	}

	@Asynchronous
	@Override
	public final void reset() {
		logging.debug("Resetting " + this);
		synchronized (logging) {
			while (countDownLatch.getCount() > 0) {
				goOn();
			}
			countDownLatch = new CountDownLatch(numberOfActions);
		}
	}


}
