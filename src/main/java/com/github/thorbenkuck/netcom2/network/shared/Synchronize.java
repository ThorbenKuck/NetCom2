package com.github.thorbenkuck.netcom2.network.shared;

public interface Synchronize extends Awaiting {

	Synchronize EMPTY_SYNCHRONIZE = new EmptySynchronize();

	static Synchronize empty() {
		return EMPTY_SYNCHRONIZE;
	}

	void error();

	void goOn();

	void reset();
}
