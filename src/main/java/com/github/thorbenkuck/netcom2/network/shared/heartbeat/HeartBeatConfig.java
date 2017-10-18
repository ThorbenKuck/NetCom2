package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

class HeartBeatConfig<T> {

	private Predicate<T> runningPredicate;
	private Predicate<T> activePredicates;
	private int times = 1;
	private long delay = 1;
	private TimeUnit timeUnit = TimeUnit.SECONDS;
	private boolean changed;

	public void setRunningPredicate(Predicate<T> predicate) {
		changed = true;
		runningPredicate = predicate;
	}

	public void addActivePredicate(Predicate<T> predicate) {
		changed = true;
		activePredicates = predicate;
	}

	public Predicate<T> getActivePredicates() {
		return activePredicates;
	}

	public Predicate<T> getRunningPredicates() {
		return runningPredicate;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		changed = true;
		this.times = times;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		changed = true;
		this.delay = delay;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		changed = true;
		this.timeUnit = timeUnit;
	}

	public void unsetChanged() {
		changed = false;
	}

	public boolean changed() {
		return changed;
	}

	public void clear() {
		runningPredicate = null;
		activePredicates = null;
		times = 1;
		delay = 1;
		timeUnit = TimeUnit.SECONDS;
		changed = true;
	}
}
