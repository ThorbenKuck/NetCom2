package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.exceptions.CommunicationAlreadySpecifiedException;
import de.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import de.thorbenkuck.netcom2.network.shared.User;

public interface CommunicationRegistration {

	static CommunicationRegistration create() {
		return new DefaultCommunicationRegistration();
	}

	<T> void register(Class<T> clazz, OnReceive<T> onReceive) throws CommunicationAlreadySpecifiedException;

	void unRegister(Class clazz);

	boolean isRegistered(Class clazz);

	<T> void trigger(Class<T> clazz, User user, Object o) throws CommunicationNotSpecifiedException;

}
