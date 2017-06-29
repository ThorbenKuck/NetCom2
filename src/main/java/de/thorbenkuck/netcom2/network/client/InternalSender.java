package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import de.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import de.thorbenkuck.netcom2.network.shared.clients.Client;

import java.util.Observer;

interface InternalSender extends Sender {

	static InternalSender create(Client client, Cache cache) {
		return new SenderImpl(client, cache);
	}

	<T> CacheObserver<T> deleteObserver(Class clazz);

	<T> CacheObserver<T> getObserver(Class<T> clazz);
}
