package de.thorbenkuck.netcom2.network.shared;

public interface Synchronize extends Awaiting {

	void error();

	void goOn();

	void reset();

}
