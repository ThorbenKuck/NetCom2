package com.github.thorbenkuck.netcom2.network.shared;

public interface CommunicationRegistration {
	static CommunicationRegistration open() {
		return new NativeCommunicationRegistration();
	}
}
