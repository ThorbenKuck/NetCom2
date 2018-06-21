package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionResponse;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

public class ServerDefaultCommunication {

	private static final Logging logging = Logging.unified();

	public static void applyTo(ServerStart serverStart) {
		CommunicationRegistration communicationRegistration = serverStart.getCommunicationRegistration();

		communicationRegistration.register(NewConnectionRequest.class)
				.addFirst(new NewConnectionRequestHandler());
		communicationRegistration.register(NewConnectionInitializer.class)
				.addFirst(new NewConnectionInitializerHandler());
		communicationRegistration.register(NewConnectionResponse.class)
				.addFirst(new NewConnectionResponseHandler());
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
			logging.trace("Storing Connection");
			connectionContext.store();

			logging.trace("Starting to perform dangerous raw write ..");
			connectionContext.flush(newConnectionInitializer);
			logging.info("Successfully wrote NewConnectionInitializer to the requesting client");
		}
	}

	private static final class NewConnectionResponseHandler implements OnReceiveTriple<NewConnectionResponse> {

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
			logging.trace("Creating arbitrary ClientID");
			ClientID clientID = ClientID.create();

			logging.trace("Starting to perform dangerous raw write ..");
			connectionContext.flush(new Ping(clientID));
			logging.trace("Successfully Send Ping to Client!");
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
		}
	}
}
