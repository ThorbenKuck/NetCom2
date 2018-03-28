package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.exceptions.ConnectionCreationFailedException;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This Class handles a {@link NewConnectionRequest}, received from the ServerStart
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.client.NewConnectionResponseHandlerTest")
class NewConnectionResponseHandler implements OnReceiveSingle<NewConnectionRequest> {

	private final Logging logging = Logging.unified();
	private final Client client;
	private final ClientConnector clientConnector;
	private final SocketFactory socketFactory;
	private final Sender sender;

	@APILevel
	NewConnectionResponseHandler(final Client client, final ClientConnector clientConnector,
	                             final SocketFactory socketFactory, final Sender sender) {
		this.client = client;
		this.clientConnector = clientConnector;
		this.socketFactory = socketFactory;
		this.sender = sender;
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public void accept(final NewConnectionRequest o) {
		NetCom2Utils.parameterNotNull(o);
		final Class key = o.getKey();
		final String prefix = "[" + key.getSimpleName() + "-Connection]: ";
		client.newPrimation();
		try {
			logging.debug(
					prefix + "Got response from Server to establish new Connection! Creating the new Connection...");
			logging.trace(prefix + "Creating Connection by socketFactory..");
			client.setConnection(key, clientConnector.establishConnection(key, socketFactory));
			logging.trace(prefix + "Created Connection by socketFactory!");
			logging.trace(prefix + "Listening for Handshake-Core (Ping)");
			client.primed().synchronize();
			logging.trace(prefix + "Received default ping! Client is now primed!");
			logging.debug(prefix + "Sending a NewConnectionInitializer over the new Connection");
			final List<ClientID> toRemove = new ArrayList<>();
			for (ClientID toDeleteID : client.getFalseIDs()) {
				logging.trace(prefix + "Requesting deletion of old key: " + toDeleteID);
				sender.objectToServer(new NewConnectionInitializer(key, client.getID(), toDeleteID), key);
				toRemove.add(toDeleteID);
				logging.trace(prefix + "Marked for deletion " + toDeleteID);
			}
			logging.trace(prefix + "Clearing false IDs from local Client");
			client.removeFalseIDs(toRemove);
			logging.info("Established new Connection to Server with key: " + key);
			if (client.isConnectionPrepared(key)) {
				client.notifyAboutPreparedConnection(key);
			}
		} catch (IOException e) {
			logging.fatal("Could not create Connection!", e);
			throw new ConnectionCreationFailedException(e);
		} catch (InterruptedException e) {
			logging.fatal("Encountered Exception while synchronizing!", e);
			logging.fatal("No fallback! Server and Client are now possibly desynchronized!");
			throw new ConnectionCreationFailedException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "NewConnectionResponseHandler{" +
				"client=" + client +
				", clientConnector=" + clientConnector +
				", socketFactory=" + socketFactory +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof NewConnectionResponseHandler)) return false;

		NewConnectionResponseHandler handler = (NewConnectionResponseHandler) o;

		return logging.equals(handler.logging) && client.equals(handler.client)
				&& clientConnector.equals(handler.clientConnector) && socketFactory.equals(handler.socketFactory)
				&& sender.equals(handler.sender);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = logging.hashCode();
		result = 31 * result + client.hashCode();
		result = 31 * result + clientConnector.hashCode();
		result = 31 * result + socketFactory.hashCode();
		result = 31 * result + sender.hashCode();
		return result;
	}
}
