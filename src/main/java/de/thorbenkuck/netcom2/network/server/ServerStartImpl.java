package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.interfaces.Factory;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.handler.ClientConnectedHandler;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ServerStartImpl implements ServerStart {

	private final List<ClientConnectedHandler> clientConnectedHandlers = new ArrayList<>();
	private final CommunicationRegistration communicationRegistration = CommunicationRegistration.create();
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final ClientList clientList = ClientList.create();
	private final DistributorRegistration registration = new DistributorRegistration();
	private final InternalDistributor distributor = InternalDistributor.create(clientList, registration);
	private final Cache cache = Cache.create();
	private final Logging logging = new NetComLogging();
	private ServerConnector serverConnector;
	private Factory<Integer, ServerSocket> serverSocketFactory;
	private boolean running = false;

	ServerStartImpl(ServerConnector serverConnector) {
		this.serverConnector = serverConnector;
		clientConnectedHandlers.add(new DefaultClientHandler(clientList, distributor, communicationRegistration, registration));
		setSocketFactory(integer -> {
			try {
				logging.debug("Creating java.net.ServerSocket(" + integer + ")");
				return new ServerSocket(integer);
			} catch (IOException e) {
				logging.catching(e);
				return null;
			}
		});
	}

	@Override
	public void launch() throws StartFailedException {
		logging.info("Starting server at port: " + serverConnector.getPort());
		try {
			new Initializer(distributor, communicationRegistration, cache, clientList).init();
			serverConnector.establishConnection(serverSocketFactory);
			logging.info("Established connection!");
			running = true;
		} catch (IOException e) {
			throw new StartFailedException(e);
		}
	}

	@Override
	public void acceptAllNextClients() throws ClientConnectionFailedException {
		try {
			while (running()) {
				acceptNextClient();
			}
			logging.info("SoftStop detected. Disconnecting ...");
		} catch (ClientConnectionFailedException e) {
			throw new ClientConnectionFailedException(e);
		} catch (Throwable t) {
			logging.catching(t);
			logging.error("Caught unexpected Throwable while accepting Clients!");
			logging.debug("Trying to at least disconnect ..");
			disconnect();
			throw new ClientConnectionFailedException(t);
		}
		disconnect();
	}

	@Override
	public void setPort(int port) {
		serverConnector = new ServerConnector(port);
	}

	@Override
	public void acceptNextClient() throws ClientConnectionFailedException {
		if (! running) {
			throw new ClientConnectionFailedException("Cannot accept Clients, if not started properly!");
		}
		ServerSocket serverSocket = serverConnector.getServerSocket();
		try {
			logging.info("Awaiting new Connection ..");
			Socket socket = serverSocket.accept();
			logging.debug("New connection established! " + socket.getInetAddress() + ":" + socket.getPort());
			logging.trace("Handling new Connection ..");
			threadPool.execute(() -> handle(socket));
		} catch (IOException e) {
			logging.error("Connection establishment failed! Aborting!", e);
			throw new ClientConnectionFailedException(e);
		}
	}

	@Override
	public void addClientConnectedHandler(ClientConnectedHandler clientConnectedHandler) {
		logging.debug("Added ClientConnectedHandler " + clientConnectedHandler);
		clientConnectedHandlers.add(clientConnectedHandler);
	}

	@Override
	public void removeClientConnectedHandler(ClientConnectedHandler clientConnectedHandler) {
		logging.debug("Removing ClientConnectedHandler " + clientConnectedHandler);
		clientConnectedHandlers.remove(clientConnectedHandler);
	}

	@Override
	public Distributor distribute() {
		return distributor;
	}

	@Override
	public Cache cache() {
		return cache;
	}

	@Override
	public void disconnect() {
		logging.trace("Trying to disconnect existing ServerSocket");
		softStop();
	}

	@Override
	public void setSocketFactory(Factory<Integer, ServerSocket> factory) {
		if (serverSocketFactory != null) {
			logging.debug("Overriding existing Factory " + serverSocketFactory + " with " + factory);
		}
		serverSocketFactory = factory;
	}

	@Override
	public final ClientList clientList() {
		return clientList;
	}

	@Override
	public final CommunicationRegistration getCommunicationRegistration() {
		return communicationRegistration;
	}

	private void handle(Socket socket) {
		Client client = null;
		for (ClientConnectedHandler clientConnectedHandler : clientConnectedHandlers) {
			if (client == null) {
				client = clientConnectedHandler.create(socket);
			}
			clientConnectedHandler.handle(client);
		}
	}

	@Override
	public void softStop() {
		logging.debug("Stopping ..");
		running = false;
	}

	@Override
	public final boolean running() {
		return running;
	}

	@Override
	public void setLogging(Logging logging) {
		NetComLogging.setLogging(logging);
	}

	@Override
	public String toString() {
		return "ServerStart{" +
				"clientConnectedHandlers=" + clientConnectedHandlers +
				", communicationRegistration=" + communicationRegistration +
				", clientList=" + clientList +
				", cache=" + cache +
				", running=" + running +
				'}';
	}

//	@Override
//	public Awaiting createNewConnection(Client client, Class key) {
//		// client.send(new NewConnectionRequest(key)).waitAt(NewConnectionResponse.class);
//		return null;
//	}
}
