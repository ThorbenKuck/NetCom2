package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
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
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
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
		 * @param connection               the first input argument
		 * @param session                  the second input argument
		 * @param newConnectionInitializer
		 */
		@Override
		public void accept(Connection connection, Session session, NewConnectionInitializer newConnectionInitializer) {
			logging.debug("Received NewConnectionInitializer");
			logging.trace("Trying to fetch Client..");
			Client client = connection.hookedClient().orElseThrow(() -> new IllegalStateException("Client not hooked to the Connection! Cannot initialize!"));
			logging.trace("Storing Connection");
			client.addConnection(connection);

			// TODO Extract in common class
			try {
				logging.trace("Starting to perform dangerous raw write ..");
				connection.write(client.objectHandler().convert(newConnectionInitializer));
				logging.info("Successfully wrote NewConnectionInitializer to the requesting client");
			} catch (SerializationFailedException e) {
				// TODO Maybe remove faulty connection or something.
				logging.error("We could not serialize the Response to initialize the new Connection! This only happens, if you manually override the NewConnectionResponse serialization!", e);
				logging.error("You are left with a faulty Connection: " + connection);
				logging.error("This cannot be reversed!");
			}
		}
	}

	private static final class NewConnectionResponseHandler implements OnReceiveTriple<NewConnectionResponse> {

		/**
		 * Performs this operation on the given arguments.
		 *
		 * @param connection            the first input argument
		 * @param session               the second input argument
		 * @param newConnectionResponse
		 */
		@Override
		public void accept(Connection connection, Session session, NewConnectionResponse newConnectionResponse) {
			logging.debug("Received NewConnectionResponse");
			logging.trace("Trying to fetch Client..");
			Client client = connection.hookedClient().orElseThrow(() -> new IllegalStateException("Client not hooked to the Connection! Cannot inform the other side!"));
			logging.trace("Creating arbitrary ClientID");
			ClientID clientID = ClientID.create();

			// TODO Extract in common class
			try {
				logging.trace("Starting to perform dangerous raw write ..");
				connection.write(client.objectHandler().convert(new Ping(clientID)));
				logging.trace("Successfully Send Ping to Client!");
			} catch (SerializationFailedException e) {
				// TODO Maybe remove faulty connection or something.
				logging.error("We could not serialize the Response to initialize the new Connection! This only happens, if you manually override the NewConnectionResponse serialization!", e);
				logging.error("You are left with a faulty Connection: " + connection);
				logging.error("This cannot be reversed!");
			}
		}
	}

	private static final class PingHandler implements OnReceiveTriple<Ping> {

		/**
		 * Performs this operation on the given arguments.
		 *
		 * @param connection the first input argument
		 * @param session    the second input argument
		 * @param ping
		 */
		@Override
		public void accept(Connection connection, Session session, Ping ping) {
			logging.debug("Received Ping from Client");
			logging.trace("Finishing associated Connection");
			connection.finishSetup();
		}
	}
}
