package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.clients.Client;

import java.util.Observer;

interface InternalSender extends Sender {

	static InternalSender create(Client client, Cache cache) {
		return new SenderImpl(client, cache);
	}

	Observer deleteObserver(Class clazz);

	Observer getObserver(Class clazz);
}
