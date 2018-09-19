package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.server.ClientFactory;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class NativeUDPConnectorCore extends AbstractConnectorCore {

	private final Logging logging = Logging.unified();
	private final Value<DatagramSocket> datagramSocketValue = Value.emptySynchronized();
	private final Value<Boolean> connected = Value.synchronize(false);
	private final Synchronize handleSynchronize = Synchronize.createDefault();

	public NativeUDPConnectorCore(ClientFactory clientFactory) {
		super(clientFactory);
		logging.instantiated(this);
	}

	private void registerConnected(DatagramSocket socket) throws IOException {
		Connection connection = Connection.udp(socket);
		// Assume the DefaultConnection
		// This will not always be true.
		// However, the chain of
		// initial messages will fix this
		connection.setIdentifier(DefaultConnection.class);

		Client client = createClient();
		connection.hook(ConnectionContext.combine(client, connection));

		logging.trace("Registering Connection to EventLoop");
		getCurrentEventLoop().register(connection);
	}

	@Override
	protected EventLoop createEventLoop() {
		return EventLoop.openBlocking();
	}

	@Override
	protected void close() throws IOException {
		datagramSocketValue.get().close();
		handleSynchronize.goOn();
	}

	@Override
	public void clear() {
		datagramSocketValue.clear();
	}

	@Override
	public void establishConnection(SocketAddress socketAddress) throws StartFailedException {
		if (connected.get()) {
			return;
		}
		try {
			DatagramSocket datagramSocket = new DatagramSocket(socketAddress);
			datagramSocketValue.set(datagramSocket);

			connected.set(true);
		} catch (IOException e) {
			throw new StartFailedException(e);
		}
	}

	@Override
	public void handleNext() throws ClientConnectionFailedException {
		try {
			registerConnected(datagramSocketValue.get());
		} catch (IOException e) {
			throw new ClientConnectionFailedException(e);
		}

		try {
			handleSynchronize.synchronize();
		} catch (InterruptedException e) {
			throw new ClientConnectionFailedException("Interrupted while awaiting next Client", e);
		}
	}
}
