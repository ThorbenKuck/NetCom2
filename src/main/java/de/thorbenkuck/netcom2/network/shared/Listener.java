package de.thorbenkuck.netcom2.network.shared;

import java.util.concurrent.CountDownLatch;

public class Listener<T extends Class> implements ListenAndExpect<Class> {

	private Class t;
	private CountDownLatch countDownLatch = new CountDownLatch(1);
	private boolean removable = true;

	public Listener(T t) {
		if (t == null) {
			throw new NullPointerException();
		}
		this.t = t;
	}

	@Override
	public final void andAwaitReceivingOfClass(Class clazz) throws InterruptedException {
		removable = false;
		this.t = clazz;
		await();
	}

	private void await() throws InterruptedException {
		countDownLatch.await();
	}

	@Override
	public final void tryAccept(Class clazz) {
		if (t.equals(clazz)) {
			trigger();
		}
	}

	@Override
	public final boolean remove() {
		return removable;
	}

	@Override
	public final boolean acceptable(Object o) {
		return o != null && o.getClass().equals(t);
	}

	protected final void trigger() {
		countDownLatch.countDown();
		removable = true;
	}

	public String toString() {
		return "Listening for Class: " + t;
	}
}
