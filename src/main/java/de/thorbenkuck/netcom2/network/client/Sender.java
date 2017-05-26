package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.network.shared.cache.CacheObserver;

public interface Sender {

	void objectToServer(Object o);

	<T> void registrationToServer(Class<T> clazz, CacheObserver<T> observer);

	void unRegistrationToServer(Class clazz);
}
