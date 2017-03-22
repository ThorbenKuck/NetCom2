package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
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
	private final ClientList clientList = new ClientList();
	private final DistributorRegistration registration = new DistributorRegistration();
	private final Distributor distributor = new Distributor(clientList, registration);
	private final Cache cache = Cache.get();
	private ServerConnector serverConnector;
	private Factory<Integer, ServerSocket> serverSocketFactory;
	private boolean running = false;
	private LoggingUtil logging = new LoggingUtil();

	ServerStartImpl(ServerConnector serverConnector) {
		this.serverConnector = serverConnector;
		clientConnectedHandlers.add(new DefaultClientHandler(clientList, distributor, communicationRegistration, registration));
		setSocketFactory(integer -> {
			try {
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
			running = true;
			logging.debug("Opened ServerSocket!");
		} catch (IOException e) {
			throw new StartFailedException(e);
		}
	}

	@Override
	public void acceptClients() throws ClientConnectionFailedException {
		ServerSocket serverSocket = serverConnector.getServerSocket();
		while (running()) {
			try {
				logging.debug("Awaiting new Clients ..");
				Socket socket = serverSocket.accept();
				logging.debug("Client connected! " + socket.getInetAddress() + ":" + socket.getPort());
				threadPool.execute(() -> handle(socket));
			} catch (IOException e) {
				logging.error("Client-Connection failed! Aborting!", e);
				throw new ClientConnectionFailedException(e);
			}
		}
		try {
			serverConnector.disconnect();
		} catch (IOException e) {
			logging.catching(e);
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
	public void addClientConnectedHandler(ClientConnectedHandler clientConnectedHandler) {
		clientConnectedHandlers.add(clientConnectedHandler);
	}

	@Override
	public void removeClientConnectedHandler(ClientConnectedHandler clientConnectedHandler) {
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
	public void setSocketFactory(Factory<Integer, ServerSocket> factory) {
		serverSocketFactory = factory;
	}

	@Override
	public CommunicationRegistration getCommunicationRegistration() {
		return communicationRegistration;
	}

	@Override
	public void softStop() {
		logging.debug("stopping ...");
		running = false;
	}

	@Override
	public boolean running() {
		return running;
	}

	@Override
	public void setLogging(Logging logging) {
		LoggingUtil.setLogging(logging);
	}
}
