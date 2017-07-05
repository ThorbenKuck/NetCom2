package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import de.thorbenkuck.netcom2.network.shared.clients.Client;

interface InternalSender extends Sender {

	static InternalSender create(Client client, Cache cache) {
		return new SenderImpl(client, cache);
	}

	<T> void addPendingObserver(Class<T> clazz, CacheObserver<T> observer);

	<T> CacheObserver<T> removePendingObserver(Class clazz);

	<T> CacheObserver<T> getPendingObserver(Class<T> clazz);
}
