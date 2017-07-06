package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.annotations.Asynchronous;
import de.thorbenkuck.netcom2.annotations.Synchronized;
import de.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.interfaces.Factory;
import de.thorbenkuck.netcom2.network.handler.ClientConnectedHandler;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.Synchronize;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Synchronized
class ServerStartImpl implements ServerStart {

	private final List<ClientConnectedHandler> clientConnectedHandlers = new ArrayList<>();
	private final CommunicationRegistration communicationRegistration = CommunicationRegistration.create();
	private final ClientList clientList = ClientList.create();
	private final DistributorRegistration registration = new DistributorRegistration();
	private final InternalDistributor distributor = InternalDistributor.create(clientList, registration);
	private final Cache cache = Cache.create();
	private final Lock threadPoolLock = new ReentrantLock();
	private ExecutorService threadPool = Executors.newCachedThreadPool();
	private Logging logging = Logging.unified();
	private ServerConnector serverConnector;
	private Factory<Integer, ServerSocket> serverSocketFactory;
	private boolean running = false;

	ServerStartImpl(ServerConnector serverConnector) {
		logging.debug("Instantiating ServerStart ..");
		this.serverConnector = serverConnector;
		logging.trace("Adding DefaultClientHandler ..");
		addClientConnectedHandler(new DefaultClientHandler(clientList, distributor, communicationRegistration, registration));
		logging.trace("Setting DefaultServerSocketFactory ..");
		setServerSocketFactory(new DefaultServerSocketFactory());
	}

	@Override
	public synchronized void launch() throws StartFailedException {
		if (running) {
			logging.error("ServerStart is already launched!");
			throw new StartFailedException("ServerStart is already launched!");
		}
		logging.info("Starting server at port: " + serverConnector.getPort());
		try {
			logging.trace("Initializing connection to port: " + serverConnector.getPort());
			new Initializer(distributor, communicationRegistration, cache, clientList).init();
			logging.trace("Establishing Connection ..");
			serverConnector.establishConnection(serverSocketFactory);
			logging.info("Established connection at port: " + serverConnector.getPort());
			running = true;
		} catch (IOException e) {
			logging.fatal("Could not start! Shutting down safely ..", e);
			StartFailedException startFailedException = new StartFailedException(e);
			try {
				serverConnector.shutDown();
			} catch (IOException e1) {
				logging.error("Encountered Exception, while shutting down Connection!", e1);
				startFailedException.addSuppressed(e1);
			}
			throw startFailedException;
		} catch (Throwable throwable) {
			logging.fatal("Failed to start Server, because of an unexpected Throwable", throwable);
			throw new StartFailedException(throwable);
		}
	}

	@Override
	public void acceptAllNextClients() throws ClientConnectionFailedException {
		logging.debug("Starting to accept all next Clients ..");
		try {
			while (running()) {
				acceptNextClient();
			}
			logging.info("SoftStop detected. Disconnecting ...");
		} catch (ClientConnectionFailedException e) {
			throw new ClientConnectionFailedException(e);
		} catch (Throwable t) {
			logging.fatal("Caught unexpected Throwable while accepting Clients!", t);
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
			throw new ClientConnectionFailedException("Cannot accept Clients, if not launched!");
		}
		logging.debug("Accepting next Client.");
		ServerSocket serverSocket = serverConnector.getServerSocket();
		try {
			logging.info("Awaiting new Connection ..");
			Socket socket = serverSocket.accept();
			logging.debug("New connection established! " + socket.getInetAddress() + ":" + socket.getPort());
			logging.trace("Handling new Connection ..");
			try {
				threadPoolLock.lock();
				threadPool.execute(() -> handle(socket));
			} finally {
				threadPoolLock.unlock();
			}
		} catch (IOException e) {
			logging.error("Connection establishment failed! Aborting!");
			throw new ClientConnectionFailedException(e);
		}
	}

	@Override
	public void addClientConnectedHandler(ClientConnectedHandler clientConnectedHandler) {
		logging.debug("Added ClientConnectedHandler " + clientConnectedHandler);
		synchronized (clientConnectedHandlers) {
			clientConnectedHandlers.add(clientConnectedHandler);
		}
	}

	@Override
	public void removeClientConnectedHandler(ClientConnectedHandler clientConnectedHandler) {
		logging.debug("Removing ClientConnectedHandler " + clientConnectedHandler);
		synchronized (clientConnectedHandlers) {
			clientConnectedHandlers.remove(clientConnectedHandler);
		}
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
		logging.trace("Requesting disconnect of existing ServerSocket");
		hardStop();
	}

	@Override
	public void setServerSocketFactory(Factory<Integer, ServerSocket> factory) {
		if (serverSocketFactory != null) {
			logging.debug("Overriding existing Factory " + serverSocketFactory + " with " + factory);
		}
		logging.trace("Set ServerSocketFactory to " + factory);
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

	@Override
	public void setExecutorService(ExecutorService executorService) {
		try {
			threadPoolLock.lock();
			this.threadPool = executorService;
		} finally {
			threadPoolLock.unlock();
		}
	}

	/**
	 *
	 * @param socket
	 */
	@Asynchronous
	private void handle(Socket socket) {
		logging.debug("Handling new Socket: " + socket);
		Client client = null;
		logging.trace("Requesting handling at clientConnectedHandlers ..");
		List<ClientConnectedHandler> clientConnectedHandlerList;
		synchronized (clientConnectedHandlers) {
			clientConnectedHandlerList = new ArrayList<>(clientConnectedHandlers);
		}
		for (ClientConnectedHandler clientConnectedHandler : clientConnectedHandlerList) {
			if (client == null) {
				logging.trace("Asking ClientConnectedHandler " + clientConnectedHandler + " to create Client ..");
				client = clientConnectedHandler.create(socket);
				if (client != null) {
					logging.trace("ClientConnectedHandler " + clientConnectedHandler + " successfully created Client! Blocking access to client-creation.");
				}
			}
			logging.trace("Asking ClientConnectedHandler " + clientConnectedHandler + " to handle Client ..");
			clientConnectedHandler.handle(client);
		}
	}

	private void hardStop() {
		logging.info("Shutting threadPool down forcefully! Expect interrupted Exceptions!");
		threadPool.shutdownNow();
		running = false;
	}

	@Asynchronous
	@Override
	public void softStop() {
		logging.debug("Stopping ..");
		logging.trace("Notifying about stop ..");
		running = false;
		logging.trace("Shutting down ThreadPool ..");
		try {
			threadPoolLock.lock();
			threadPool.shutdown();
			try {
				logging.trace("Awaiting termination of all Threads ..");
				threadPool.awaitTermination(20, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logging.error("Exception while awaiting termination!", e);
			}
		} finally {
			threadPoolLock.unlock();
		}
		logging.trace("Shutdown request completed!");
	}

	@Override
	public final boolean running() {
		return running;
	}

	@Override
	public void setLogging(Logging logging) {
		this.logging.trace("Updating logging ..");
		this.logging = logging;
		this.logging.debug("Updated logging!");
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

	@Asynchronous
	@Override
	public Awaiting createNewConnection(Session session, Class key) {
		logging.debug("Trying to create Connection " + key + " for Session " + session);
		logging.trace("Getting Client from ClientList ..");
		Optional<Client> clientOptional = clientList.getClient(session);
		if (! clientOptional.isPresent()) {
			logging.warn("Could not locate Client for Session: " + session);
			return Synchronize.empty();
		}
		return clientOptional.get().createNewConnection(key);
	}
}
