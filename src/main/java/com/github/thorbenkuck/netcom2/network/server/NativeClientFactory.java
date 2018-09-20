package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientConnectedHandler;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

final class NativeClientFactory implements ClientFactory {

	private final CommunicationRegistration communicationRegistration;
	private final Pipeline<Client> clientPipeline = Pipeline.unifiedCreation();
	private final Logging logging = Logging.unified();

	NativeClientFactory(final CommunicationRegistration communicationRegistration) {
		this.communicationRegistration = communicationRegistration;
		logging.instantiated(this);
	}

	private void apply(final Client client) {
		NetComThreadPool.submitTask(new Runnable() {

			@Override
			public final void run() {
				logging.debug("Acquiring ClientConnectedPipeline");
				synchronized (clientPipeline) {
					logging.trace("Acquired ClientPipeline. Applying ConnectedClient");
					clientPipeline.apply(client);
				}
			}

			@Override
			public final String toString() {
				return "Task{Apply connectedPipeline}";
			}
		});
	}

	@Override
	public final Client produce() {
		final Client client = Client.create(communicationRegistration);
		client.setSession(Session.open(client));

		client.addPrimedCallback(this::apply);
		return client;
	}

	@Override
	public final void addClientConnectedHandler(final ClientConnectedHandler clientConnectedHandler) {
		synchronized (clientPipeline) {
			clientPipeline.addLast(clientConnectedHandler);
		}
	}

	@Override
	public final void removeClientConnectedHandler(final ClientConnectedHandler clientConnectedHandler) {
		synchronized (clientPipeline) {
			clientPipeline.remove(clientConnectedHandler);
		}
	}
}
