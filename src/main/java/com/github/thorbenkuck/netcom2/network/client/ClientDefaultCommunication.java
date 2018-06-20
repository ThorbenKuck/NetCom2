package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionResponse;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

public class ClientDefaultCommunication {

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
			// TODO open a new Connection through the ClientStart (old Design)
		}
	}

	private static final class NewConnectionInitializerHandler implements OnReceiveTriple<NewConnectionInitializer> {

		private final Logging logging = Logging.unified();

		/**
		 * Performs this operation on the given arguments.
		 *
		 * @param session                  the first input argument
		 * @param newConnectionInitializer the second input argument
		 */
		@Override
		public void accept(Connection connection, Session session, NewConnectionInitializer newConnectionInitializer) {
			Client client = connection.hookedClient().orElseThrow(() -> new IllegalStateException("Client not hooked to the Connection! Cannot initialize!"));
			NewConnectionResponse response = new NewConnectionResponse();
			client.addConnection(connection);

			// TODO Extract in common class
			try {
				connection.write(client.objectHandler().convert(response));
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
			Client client = connection.hookedClient().orElseThrow(() -> new IllegalStateException("No Client hooked to Connection!"));
			if (client.getId() == null) {
				client.setID(ping.getClientID());
			}
			connection.finishSetup();
			client.send(ping, connection);
		}
	}
}
