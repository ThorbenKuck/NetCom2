package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import de.thorbenkuck.netcom2.interfaces.Pipeline;
import de.thorbenkuck.netcom2.network.shared.User;

public interface CommunicationRegistration {

	static CommunicationRegistration create() {
		return new DefaultCommunicationRegistration();
	}

	<T> Pipeline<T> register(Class<T> clazz);

	void unRegister(Class clazz);

	boolean isRegistered(Class clazz);

	<T> void trigger(Class<T> clazz, User user, Object o) throws CommunicationNotSpecifiedException;

	void addDefaultCommunicationHandler(DefaultCommunicationHandler defaultCommunicationHandler);

}
