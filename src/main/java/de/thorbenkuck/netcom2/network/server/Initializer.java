package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.server.communication.RegisterRequestReceiveHandler;
import de.thorbenkuck.netcom2.network.server.communication.UnRegisterRequestReceiveHandler;
import de.thorbenkuck.netcom2.network.shared.cache.*;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.*;

import java.util.Observable;
import java.util.Optional;

class Initializer {

	private final InternalDistributor distributor;
	private final CommunicationRegistration communicationRegistration;
	private final Cache cache;
	private final ClientList clients;
	private Logging logging = new NetComLogging();

	Initializer(InternalDistributor distributor, CommunicationRegistration communicationRegistration,
				Cache cache, ClientList clients) {
		this.distributor = distributor;
		this.communicationRegistration = communicationRegistration;
		this.cache = cache;
		this.clients = clients;
	}

	void init() {
		logging.trace("Creating internal dependencies");
		register();
		setObserver();
	}

	private void register() {
		communicationRegistration.register(RegisterRequest.class)
				.addFirst(new RegisterRequestReceiveHandler(distributor.getDistributorRegistration(), cache))
				.withRequirement((session, registerRequest) -> ! distributor.getDistributorRegistration().getRegistered(registerRequest.getCorrespondingClass()).contains(session));
		communicationRegistration.register(UnRegisterRequest.class)
				.addLast(new UnRegisterRequestReceiveHandler(distributor.getDistributorRegistration()))
				.withRequirement((session, registerRequest) -> distributor.getDistributorRegistration().getRegistered(registerRequest.getCorrespondingClass()).contains(session));

		// TODO auslagern!
		communicationRegistration.register(Ping.class)
				.addLast((session, ping) -> {
					logging.debug("Ping received from Client");
					System.out.println(clients);
					System.out.println(session);
					Optional<Client> clientOptional = clients.getClient(session);
					clientOptional.ifPresent(client -> {
						if (client.getID().equals(ping.getId())) {
							logging.debug("Acknowledged!");
							client.triggerPrimation();
							logging.info("Handshake with new Client Complete!");
						} else {
							logging.warn("Detected malicious activity at Client: " + client + "!");
							logging.debug("Forcing disconnect now...");
							client.disconnect();
						}
					});
				});
		// TODO auslagern!
		communicationRegistration.register(NewConnectionRequest.class)
				.addLast(((session, o) -> {
					logging.info("Client of Session " + session + " requested new Connection with key: " + o.getKey());
					logging.trace("Acknowledging request..");
					session.send(o);
				}));
		// TODO auslagern!
		communicationRegistration.register(NewConnectionInitializer.class)
				.addLast((connection, session, newConnectionInitializer) -> {
					logging.debug("Processing NewConnectionInitializer");
					String identifier = newConnectionInitializer.getID() + "@" + newConnectionInitializer.getConnectionKey();
					logging.debug("Received ConnectionInitializer for: " + identifier);
					logging.trace("[" + identifier + "]: Verifying Client ..");
					Optional<Client> clientOptional = clients.getClient(newConnectionInitializer.getID());
					Optional<Client> toDeleteClientOptional = clients.getClient(newConnectionInitializer.getToDeleteID());
					if (clientOptional.isPresent() && toDeleteClientOptional.isPresent()) {
						logging.trace("[" + identifier + "]: Client exists!");
						Client client = clientOptional.get();
						Client toDelete = toDeleteClientOptional.get();
						logging.trace("[" + identifier + "]: Setting new Connection ..");
						client.setConnection(newConnectionInitializer.getConnectionKey(), connection);
						connection.setSession(client.getSession());
						logging.trace("[" + identifier + "]: New Connection is now usable under the key: " + newConnectionInitializer.getConnectionKey());
						logging.trace("[" + identifier + "]: Acknowledging newly initialized Connection..");
						connection.writeObject(newConnectionInitializer);
						logging.trace("[" + identifier + "]: Removing duplicate..");
						clients.remove(toDelete);
					} else {
						logging.warn("[" + identifier + "]: Needed to find 2 Clients! Found: " + clientOptional + " and " + toDeleteClientOptional);
					}
				});

	}

	private void setObserver() {
		logging.trace("Adding internal CacheObserver ..");
		cache.addCacheObserver(new ObserverSender(distributor));
	}

	private class ObserverSender extends AbstractCacheObserver {

		private Distributor distributor;

		ObserverSender(Distributor distributor) {
			this.distributor = distributor;
		}

		@Override
		public void newEntry(NewEntryEvent newEntryEvent, Observable observable) {
			distributor.toRegistered(newEntryEvent.getObject());
		}

		@Override
		public void updatedEntry(UpdatedEntryEvent updatedEntryEvent, Observable observable) {
			distributor.toRegistered(updatedEntryEvent.getObject());
		}

		@Override
		public void deletedEntry(DeletedEntryEvent deletedEntryEvent, Observable observable) {
			NetComLogging.getLogging().error("TODO");
		}
	}
}