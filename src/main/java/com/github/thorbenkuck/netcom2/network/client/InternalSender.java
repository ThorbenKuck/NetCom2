package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

interface InternalSender extends Sender {

	static InternalSender create(final Client client) {
		return new SenderImpl(client);
	}

	<T> void addPendingObserver(final Class<T> clazz, final CacheObserver<T> observer);

	<T> CacheObserver<T> removePendingObserver(final Class clazz);

	<T> CacheObserver<T> getPendingObserver(final Class<T> clazz);
}
