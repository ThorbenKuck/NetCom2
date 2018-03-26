package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This Class handles an {@link NewConnectionInitializer}, received from the ServerStart.
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
class NewConnectionInitializerHandler implements OnReceiveTriple<NewConnectionInitializer> {

	private final Logging logging = Logging.unified();
	private final Client client;

	@APILevel
	NewConnectionInitializerHandler(final Client client) {
		this.client = client;
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public void accept(final Connection connection, final Session session,
	                   final NewConnectionInitializer newConnectionInitializer) {
		NetCom2Utils.assertNotNull(connection, newConnectionInitializer);
		logging.info("Setting new Connection to Key " + newConnectionInitializer.getConnectionKey());
		client.setConnection(newConnectionInitializer.getConnectionKey(), connection);
		client.removeFalseID(newConnectionInitializer.getToDeleteID());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "NewConnectionInitializerHandler{" +
				"client=" + client +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof NewConnectionInitializerHandler)) return false;

		NewConnectionInitializerHandler that = (NewConnectionInitializerHandler) o;

		if (!logging.equals(that.logging)) return false;
		return client.equals(that.client);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = logging.hashCode();
		result = 31 * result + client.hashCode();
		return result;
	}
}
