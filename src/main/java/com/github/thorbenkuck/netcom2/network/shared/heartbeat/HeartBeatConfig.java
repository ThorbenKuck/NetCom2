package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@APILevel
@Synchronized
class HeartBeatConfig<T> {

	private Predicate<T> runningPredicate;
	private Predicate<T> activePredicates;
	private int times = 1;
	private long delay = 1;
	private TimeUnit timeUnit = TimeUnit.SECONDS;
	private boolean changed;

	void setRunningPredicate(Predicate<T> predicate) {
		changed = true;
		runningPredicate = predicate;
	}

	void addActivePredicate(Predicate<T> predicate) {
		changed = true;
		activePredicates = predicate;
	}

	Predicate<T> getActivePredicates() {
		return activePredicates;
	}

	Predicate<T> getRunningPredicates() {
		return runningPredicate;
	}

	int getTimes() {
		return times;
	}

	void setTimes(int times) {
		changed = true;
		this.times = times;
	}

	long getDelay() {
		return delay;
	}

	void setDelay(long delay) {
		changed = true;
		this.delay = delay;
	}

	TimeUnit getTimeUnit() {
		return timeUnit;
	}

	void setTimeUnit(TimeUnit timeUnit) {
		changed = true;
		this.timeUnit = timeUnit;
	}

	void unsetChanged() {
		changed = false;
	}

	boolean changed() {
		return changed;
	}

	void clear() {
		runningPredicate = null;
		activePredicates = null;
		times = 1;
		delay = 1;
		timeUnit = TimeUnit.SECONDS;
		changed = true;
	}
}
