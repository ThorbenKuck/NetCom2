package de.thorbenkuck.netcom2.network.client;

import java.util.Observer;

public interface Sender {

	void objectToServer(Object o);

	void registrationToServer(Class clazz, Observer observer);

	void unRegistrationToServer(Class clazz);
}
