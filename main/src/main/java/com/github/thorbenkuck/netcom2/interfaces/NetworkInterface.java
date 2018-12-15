package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;

public interface NetworkInterface extends Launch, Loggable {

	Cache cache();

	CommunicationRegistration getCommunicationRegistration();

}
