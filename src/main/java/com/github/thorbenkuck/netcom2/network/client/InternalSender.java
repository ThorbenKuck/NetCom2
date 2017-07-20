package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

interface InternalSender extends Sender {

	static InternalSender create(Client client, Cache cache) {
		return new SenderImpl(client, cache);
	}

	<T> void addPendingObserver(Class<T> clazz, CacheObserver<T> observer);

	<T> CacheObserver<T> removePendingObserver(Class clazz);

	<T> CacheObserver<T> getPendingObserver(Class<T> clazz);
}
