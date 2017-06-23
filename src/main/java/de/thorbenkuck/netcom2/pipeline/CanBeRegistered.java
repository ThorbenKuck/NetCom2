package de.thorbenkuck.netcom2.pipeline;

public interface CanBeRegistered {

	default void onUnRegistration() {
	}

	default void onRegistration() {
	}

}
