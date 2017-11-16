package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.clients.ReceiveOrSendSynchronization;

public interface Sender {

	ReceiveOrSendSynchronization objectToServer(Object o);

	ReceiveOrSendSynchronization objectToServer(Object o, Connection connection);

	ReceiveOrSendSynchronization objectToServer(Object o, Class connectionKey);

	<T> ReceiveOrSendSynchronization registrationToServer(Class<T> clazz, CacheObserver<T> observer);

	<T> ReceiveOrSendSynchronization registrationToServer(Class<T> clazz, CacheObserver<T> observer,
														  Connection connection);

	<T> ReceiveOrSendSynchronization registrationToServer(Class<T> clazz, CacheObserver<T> observer,
														  Class connectionKey);

	<T> ReceiveOrSendSynchronization unRegistrationToServer(Class<T> clazz);

	<T> ReceiveOrSendSynchronization unRegistrationToServer(Class<T> clazz, Connection connection);

	<T> ReceiveOrSendSynchronization unRegistrationToServer(Class<T> clazz, Class connectionKey);

	void reset();

}
