package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import de.thorbenkuck.netcom2.exceptions.ServerHasToBeStartedException;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.interfaces.Factory;
import de.thorbenkuck.netcom2.logging.LoggingUtil;
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
	private final Logging logging = new LoggingUtil();
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
			new Initializer(distributor, communicationRegistration, cache).init();
			serverConnector.establishConnection(serverSocketFactory);
			logging.debug("Established connection!");
			running = true;
		} catch (IOException e) {
			throw new StartFailedException(e);
		}
	}

	@Override
	public void acceptAllNextClients() throws ClientConnectionFailedException {
		assertStarted();
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
		}
		disconnect();
	}

	@Override
	public void setPort(int port) {
		serverConnector = new ServerConnector(port);
	}

	@Override
	public void acceptNextClient() throws ClientConnectionFailedException {
		assertStarted();
		ServerSocket serverSocket = serverConnector.getServerSocket();
		try {
			logging.info("Awaiting new Client ..");
			Socket socket = serverSocket.accept();
			logging.debug("Client connected! " + socket.getInetAddress() + ":" + socket.getPort());
			threadPool.execute(() -> handle(socket));
		} catch (IOException e) {
			logging.error("Client-Connection failed! Aborting!", e);
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
		try {
			serverConnector.disconnect();
			softStop();
		} catch (IOException e) {
			logging.catching(e);
		}
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

	private void assertStarted() {
		if (! serverConnector.connected()) {
			throw new ServerHasToBeStartedException();
		}
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
		LoggingUtil.setLogging(logging);
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
}
