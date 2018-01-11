package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

@APILevel
interface InternalSender extends Sender {

	static InternalSender create(final Client client) {
		return new SenderImpl(client);
	}

	@APILevel
	<T> void addPendingObserver(final Class<T> clazz, final CacheObserver<T> observer);

	@APILevel
	<T> CacheObserver<T> removePendingObserver(final Class clazz);

	@APILevel
	<T> CacheObserver<T> getPendingObserver(final Class<T> clazz);
}
