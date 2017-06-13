package de.thorbenkuck.netcom2.network.shared;

import java.util.concurrent.CountDownLatch;

public abstract class AbstractSynchronize implements Synchronize {

	private final int numberOfActions;
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

	@Override
	public final void goOn() {
		countDownLatch.countDown();
	}

	@Override
	public final void reset() {
		while (countDownLatch.getCount() > 0) {
			countDownLatch.countDown();
		}
		countDownLatch = new CountDownLatch(numberOfActions);
	}

	@Override
	public final void synchronize() throws InterruptedException {
		countDownLatch.await();
	}
}
