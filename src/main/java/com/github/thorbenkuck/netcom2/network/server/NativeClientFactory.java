package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientConnectedHandler;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

class NativeClientFactory implements ClientFactory {

	private final CommunicationRegistration communicationRegistration;
	private final Pipeline<Client> clientPipeline = Pipeline.unifiedCreation();
	private final Logging logging = Logging.unified();

	NativeClientFactory(CommunicationRegistration communicationRegistration) {
		this.communicationRegistration = communicationRegistration;
		logging.instantiated(this);
	}

	private void apply(Client client) {
		NetComThreadPool.submitTask(new Runnable() {

			@Override
			public void run() {
				logging.debug("Acquiring ClientConnectedPipeline");
				synchronized (clientPipeline) {
					logging.trace("Acquired ClientPipeline. Applying ConnectedClient");
					clientPipeline.apply(client);
				}
			}

			@Override
			public String toString() {
				return "Task{Apply connectedPipeline}";
			}
		});
	}

	@Override
	public Client produce() {
		final Client client = Client.create(communicationRegistration);
		client.setSession(Session.open(client));

		client.addPrimedCallback(this::apply);
		return client;
	}

	@Override
	public void addClientConnectedHandler(ClientConnectedHandler clientConnectedHandler) {
		synchronized (clientPipeline) {
			clientPipeline.addLast(clientConnectedHandler);
		}
	}

	@Override
	public void removeClientConnectedHandler(ClientConnectedHandler clientConnectedHandler) {
		synchronized (clientPipeline) {
			clientPipeline.remove(clientConnectedHandler);
		}
	}
}
