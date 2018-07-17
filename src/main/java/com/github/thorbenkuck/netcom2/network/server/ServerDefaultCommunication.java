package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionResponse;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

import java.io.IOException;
import java.util.Optional;

public class ServerDefaultCommunication {

	private static final Logging logging = Logging.unified();

	public static void applyTo(ServerStart serverStart) {
		CommunicationRegistration communicationRegistration = serverStart.getCommunicationRegistration();

		communicationRegistration.register(NewConnectionRequest.class)
				.addFirst(new NewConnectionRequestHandler());
		communicationRegistration.register(NewConnectionInitializer.class)
				.addFirst(new NewConnectionInitializerHandler());
		communicationRegistration.register(NewConnectionResponse.class)
				.addFirst(new NewConnectionResponseHandler(serverStart));
		communicationRegistration.register(Ping.class)
				.addFirst(new PingHandler());
	}

	private static final class NewConnectionRequestHandler implements OnReceive<NewConnectionRequest> {

		/**
		 * Performs this operation on the given arguments.
		 *
		 * @param session              the first input argument
		 * @param newConnectionRequest the second input argument
		 */
		@Override
		public void accept(Session session, NewConnectionRequest newConnectionRequest) {
			logging.debug("Received NewConnectionRequest. Sending back ..");
			session.send(newConnectionRequest);
			logging.info("NEW_CONNECTION > 0 > Sending NewConnectionRequest back to the client");
		}
	}

	private static final class NewConnectionInitializerHandler implements OnReceiveTriple<NewConnectionInitializer> {

		/**
		 * Performs this operation on the given arguments.
		 *
		 * @param connectionContext               the first input argument
		 * @param session                  the second input argument
		 * @param newConnectionInitializer
		 */
		@Override
		public void accept(ConnectionContext connectionContext, Session session, NewConnectionInitializer newConnectionInitializer) {
			logging.debug("Received NewConnectionInitializer");
			logging.trace("Updating identifier of connection");
			connectionContext.setIdentifier(newConnectionInitializer.getIdentifier());
			logging.trace("Storing Connection");
			connectionContext.store();

			logging.trace("Starting to perform dangerous raw write ..");
			connectionContext.flush(newConnectionInitializer);
			logging.info("NEW_CONNECTION > 1 > Successfully wrote NewConnectionInitializer for " + newConnectionInitializer.getIdentifier());
		}
	}

	private static final class NewConnectionResponseHandler implements OnReceiveTriple<NewConnectionResponse> {

		private final ClientList clientList;

		private NewConnectionResponseHandler(ServerStart serverStart) {
			this.clientList = serverStart.clientList();
		}

		/**
		 * Performs this operation on the given arguments.
		 *
		 * @param connectionContext            the first input argument
		 * @param session               the second input argument
		 * @param newConnectionResponse
		 */
		@Override
		public void accept(ConnectionContext connectionContext, Session session, NewConnectionResponse newConnectionResponse) {
			logging.debug("Received NewConnectionResponse");
			logging.debug("This involves information received from the Client! Testing for malicious activity");
			ClientID clientID;
			if (newConnectionResponse.getClientID() == null) {
				logging.trace("Creating new ClientID");
				clientID = ClientID.create();
			} else {
				logging.trace("The Client appears to have already been connected. Searching for the correct Client ..");
				Optional<Client> correctClientOptional = clientList.getClient(newConnectionResponse.getClientID());
				logging.trace("Performing sanity-check on fetched Client");
				if (!correctClientOptional.isPresent()) {
					try {
						logging.warn("<SECURITY> Malicious activity detected!");
						logging.debug("Killing ConnectionContext");
						connectionContext.kill();
					} catch (IOException e) {
						logging.catching(e);
					}
					return;
				}

				Client correctClient = correctClientOptional.get();
				clientID = correctClient.getID();

				connectionContext.applyTo(correctClient);
			}

			connectionContext.updateClientID(clientID);

			logging.trace("Starting to perform raw write of the Ping..");
			connectionContext.flush(new Ping(clientID));
			logging.info("NEW_CONNECTION > 2 > Successfully wrote Ping for the Connection " + connectionContext.getIdentifier());
		}
	}

	private static final class PingHandler implements OnReceiveTriple<Ping> {

		/**
		 * Performs this operation on the given arguments.
		 *
		 * @param connectionContext the first input argument
		 * @param session    the second input argument
		 * @param ping
		 */
		@Override
		public void accept(ConnectionContext connectionContext, Session session, Ping ping) {
			logging.debug("Received Ping back from Client");
			logging.trace("Finishing associated Connection");
			connectionContext.finishConnect();
			logging.info("NEW_CONNECTION > 3 > Connection " + connectionContext.getIdentifier() + " is Successfully established!");
		}
	}
}
