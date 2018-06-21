package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionResponse;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

public class ClientDefaultCommunication {

	private static final Logging logging = Logging.unified();

	public static void applyTo(ClientStart clientStart) {
		CommunicationRegistration communicationRegistration = clientStart.getCommunicationRegistration();
		communicationRegistration.register(NewConnectionRequest.class)
				.addFirst(new NewConnectionRequestHandler());
		communicationRegistration.register(NewConnectionInitializer.class)
				.addFirst(new NewConnectionInitializerHandler());
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
			logging.info("Trying to establish a new Connection as " + newConnectionRequest.getIdentifier());
			// TODO open a new Connection through the old Design
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
			NewConnectionResponse response = new NewConnectionResponse();

			connectionContext.flush(response);
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
			logging.debug("Received Ping from Server");
			logging.trace("Checking ClientID of ConnectionContext");
			if (connectionContext.getClientID() == null) {
				logging.trace("ClientID is null, updating based on received ClientID");
				connectionContext.updateClientID(ping.getClientID());
			}
			logging.trace("Sending ping over ConnectionContext");
			connectionContext.send(ping);
			logging.trace("Finishing Connect of ConnectionContext");
			connectionContext.finishConnect();
		}
	}
}
