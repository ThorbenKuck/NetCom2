package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientDisconnectedHandler;

public class Example implements ClientDisconnectedWrapper {

	private final ObjectRepository objectRepository;

	public Example(ObjectRepository objectRepository) {
		this.objectRepository = objectRepository;
	}

	@Override
	public void apply(ServerStart serverStart, ObjectRepository repository) {
		serverStart.addClientConnectedHandler(client -> client.addDisconnectedHandler(new InnerDisconnectedHandler(objectRepository)));
	}

	@Override
	public void apply(ClientStart clientStart, ObjectRepository repository) {
		clientStart.addDisconnectedHandler(new InnerDisconnectedHandler(objectRepository));
	}

	private class InnerDisconnectedHandler implements ClientDisconnectedHandler {

		private final ObjectRepository objectRepository;

		private InnerDisconnectedHandler(ObjectRepository objectRepository) {
			this.objectRepository = objectRepository;
		}

		@Override
		public void accept(Client client) {
			// Foo
		}
	}
}
