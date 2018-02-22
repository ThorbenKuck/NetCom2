package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

/**
 * This interface is package private by design, not by error.
 * <p>
 * It should not be made public nor should it be used outside of NetCom2!
 * This allows internal dependencies to add Observer, which are to be used within the Cache and wait for corresponding
 * Communication chain, that tells the Sender, that he successfully registered to the Object
 */
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
