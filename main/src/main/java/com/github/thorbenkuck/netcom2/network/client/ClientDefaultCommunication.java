package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.exceptions.ConnectionEstablishmentFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionResponse;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;

public class ClientDefaultCommunication {

	private static final Logging logging = Logging.unified();

	public static void applyTo(ClientStart clientStart) {
		CommunicationRegistration communicationRegistration = clientStart.getCommunicationRegistration();
		communicationRegistration.register(NewConnectionRequest.class)
				.addFirst(new NewConnectionRequestHandler(clientStart));
		communicationRegistration.register(NewConnectionInitializer.class)
				.addFirst(new NewConnectionInitializerHandler());
		communicationRegistration.register(Ping.class)
				.addFirst(new PingHandler());
	}

	private static final class NewConnectionRequestHandler implements OnReceive<NewConnectionRequest> {

		private final ClientStart clientStart;

		private NewConnectionRequestHandler(ClientStart clientStart) {
			this.clientStart = clientStart;
		}

		/**
		 * Performs this operation on the given arguments.
		 *
		 * @param session              the first input argument
		 * @param newConnectionRequest the second input argument
		 */
		@Override
		public void accept(Session session, NewConnectionRequest newConnectionRequest) {
			logging.info("Trying to establish a new Connection as " + newConnectionRequest.getIdentifier());
			try {
				clientStart.newConnection(newConnectionRequest.getIdentifier());
			} catch (ConnectionEstablishmentFailedException e) {
				logging.error("Could not establish the new Connection!", e);
			}
		}
	}

	private static final class NewConnectionInitializerHandler implements OnReceiveTriple<NewConnectionInitializer> {

		/**
		 * Performs this operation on the given arguments.
		 *
		 * @param session                  the first input argument
		 * @param newConnectionInitializer the second input argument
		 */
		@Override
		public void accept(ConnectionContext connectionContext, Session session, NewConnectionInitializer newConnectionInitializer) {
			logging.debug("Initializing new Connection");
			logging.trace("Storing new Connection");
			connectionContext.store();
			logging.trace("Constructing response");
			ClientID currentID = connectionContext.getClientID();
			NewConnectionResponse response;
			if (currentID.isEmpty()) {
				response = new NewConnectionResponse(null);
			} else {
				response = new NewConnectionResponse(currentID);
			}
			connectionContext.flush(response);
		}
	}

	private static final class PingHandler implements OnReceiveTriple<Ping> {

		/**
		 * Performs this operation on the given arguments.
		 *
		 * @param connectionContext the first input argument
		 * @param session           the second input argument
		 * @param ping
		 */
		@Override
		public void accept(ConnectionContext connectionContext, Session session, Ping ping) {
			logging.debug("Received Ping from Server for " + connectionContext.getIdentifier());
			logging.trace("Checking ClientID of ConnectionContext");
			if (ClientID.isEmpty(connectionContext.getClientID())) {
				logging.trace("ClientID is null, updating based on received ClientID");
				connectionContext.updateClientID(ping.getClientID());
			}
			logging.trace("Finishing Connect of ConnectionContext");
			connectionContext.finishConnect();
			logging.trace("Sending ping over ConnectionContext");
			connectionContext.flush(ping);
		}
	}
}
