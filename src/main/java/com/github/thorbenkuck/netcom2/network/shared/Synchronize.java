package com.github.thorbenkuck.netcom2.network.shared;

public interface Synchronize extends Awaiting {

	static Synchronize empty() {
		return SynchronizeCache.EMPTY_SYNCHRONIZE;
	}

	static boolean isEmpty(Synchronize synchronize) {
		return synchronize == SynchronizeCache.EMPTY_SYNCHRONIZE;
	}

	static boolean isEmpty(Awaiting awaiting) {
		return awaiting == SynchronizeCache.EMPTY_SYNCHRONIZE;
	}

	void error();

	void goOn();

	void reset();
}
