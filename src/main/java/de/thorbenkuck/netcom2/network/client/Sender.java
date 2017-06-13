package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.network.shared.Expectable;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;

import java.util.Observer;

public interface Sender {

	Expectable objectToServer(Object o);

	Expectable objectToServer(Object o, Connection connection);

	Expectable objectToServer(Object o, Class connectionKey);

	Expectable registrationToServer(Class clazz, Observer observer);

	Expectable registrationToServer(Class clazz, Observer observer, Connection connection);

	Expectable registrationToServer(Class clazz, Observer observer, Class connectionKey);

	Expectable unRegistrationToServer(Class clazz);

	Expectable unRegistrationToServer(Class clazz, Connection connection);

	Expectable unRegistrationToServer(Class clazz, Class connectionKey);

}
