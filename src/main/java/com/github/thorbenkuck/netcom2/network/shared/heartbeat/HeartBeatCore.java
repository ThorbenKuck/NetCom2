package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

@APILevel
class HeartBeatCore<T> implements Runnable {

	private final Predicate<T> activePredicate;
	private final Predicate<T> runningPredicate;
	private final long delay;
	private T t;
	private Consumer<T> consumer;
	private volatile boolean running = false;

	@APILevel
	HeartBeatCore(Predicate<T> activePredicate, Predicate<T> runningPredicate, long times, long delay,
				  TimeUnit timeUnit) {
		this.activePredicate = activePredicate;
		this.runningPredicate = runningPredicate;
		this.delay = (long) (timeUnit.toMillis(delay) / (float) times);
	}

	@APILevel
	void setup(T t, Consumer<T> consumer) {
		this.t = t;
		this.consumer = consumer;
	}

	@Override
	public void run() {
		assertSetUp();
		start();
		while (running(t)) {
			if (active(t)) {
				call();
			}
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
				shutdown();
			}
		}
		shutdown();
	}

	private void assertSetUp() {
		if (t == null || consumer == null) {
			throw new IllegalArgumentException("Cannot start HeartBeat without Consumer or element to consume");
		}
	}

	private void start() {
		running = true;
	}

	private boolean running(T t) {
		return (runningPredicate == null || !runningPredicate.test(t)) && running;
	}

	private boolean active(T t) {
		return (activePredicate == null || activePredicate.test(t)) && running;
	}

	private void call() {
		consumer.accept(t);
	}

	@APILevel
	void shutdown() {
		running = false;
	}
}
