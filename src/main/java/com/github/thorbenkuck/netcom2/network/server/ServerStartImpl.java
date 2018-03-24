package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.interfaces.Factory;
import com.github.thorbenkuck.netcom2.interfaces.RemoteObjectRegistration;
import com.github.thorbenkuck.netcom2.network.interfaces.ClientConnectedHandler;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Synchronized
class ServerStartImpl implements ServerStart {

	@APILevel
	private final List<ClientConnectedHandler> clientConnectedHandlers = new ArrayList<>();
	private final CommunicationRegistration communicationRegistration = CommunicationRegistration.create();
	private final ClientList clientList = ClientList.create();
	@APILevel
	private final DistributorRegistration registration = new DistributorRegistration();
	private final InternalDistributor distributor = InternalDistributor.create(clientList, registration);
	private final Cache cache = Cache.create();
	@APILevel
	private final Lock threadPoolLock = new ReentrantLock();
	private final RemoteObjectRegistration remoteObjectRegistration = new RemoteObjectRegistrationImpl();
	private ExecutorService threadPool = NetCom2Utils.getNetComExecutorService();
	private Logging logging = Logging.unified();
	private ServerConnector serverConnector;
	private Factory<Integer, ServerSocket> serverSocketFactory;
	private boolean running = false;

	ServerStartImpl(@APILevel final ServerConnector serverConnector) {
		logging.debug("Instantiating ServerStart ..");
		this.serverConnector = serverConnector;
		logging.trace("Adding DefaultClientHandler ..");
		addClientConnectedHandler(
				new DefaultClientHandler(clientList, communicationRegistration, registration));
		logging.trace("Setting DefaultServerSocketFactory ..");
		setServerSocketFactory(new DefaultServerSocketFactory());
	}

	/**
	 * Handles a newly connected Socket.
	 *
	 * @param socket the Socket, that just connected
	 * @see #acceptAllNextClients()
	 * @see #acceptNextClient()
	 */
	private void handle(final Socket socket) {
		if (! running) {
			return;
		}
		logging.debug("Handling new Socket: " + socket);
		logging.trace("Requesting handling at clientConnectedHandlers ..");
		final List<ClientConnectedHandler> clientConnectedHandlerList;
		synchronized (clientConnectedHandlers) {
			clientConnectedHandlerList = new ArrayList<>(clientConnectedHandlers);
		}
		final Client client = createClient(clientConnectedHandlerList, socket);

		for (final ClientConnectedHandler clientConnectedHandler : clientConnectedHandlerList) {
			logging.trace("Asking ClientConnectedHandler " + clientConnectedHandler + " to handle Client ..");
			clientConnectedHandler.handle(client);
		}
	}

	/**
	 * Creates a Client, based on the provided ClientConnectedHandlers.
	 * <p>
	 * Utilizes all previously set ClientConnectedHandlers.
	 * <p>
	 * If ANY ClientConnectedHandler creates a Client, it will be used as the new Client. Whenever a new Client is created,
	 * it overrides the previously created Client.
	 *
	 * @param list   the ClientConnectedHandlers
	 * @param socket the Socket, the Client just connected with
	 * @return a new Instance of the Client Class
	 */
	private Client createClient(final List<ClientConnectedHandler> list, final Socket socket) {
		final List<ClientConnectedHandler> clientConnectedHandlers = list.stream()
				.filter(ClientConnectedHandler::willCreateClient)
				.collect(Collectors.toCollection(ArrayList::new));

		Client client = null;
		for (final ClientConnectedHandler clientConnectedHandler : clientConnectedHandlers) {
			logging.trace("Asking ClientConnectedHandler " + clientConnectedHandler + " to create Client ..");
			Client newClient = clientConnectedHandler.create(socket);
			if (newClient != null) {
				logging.trace("ClientConnectedHandler " + clientConnectedHandler +
						" successfully created a Client! Overriding: " + client);
				client = newClient;
			}
		}

		return client;
	}

	/**
	 * Calling this Method will Stop the Server and ALL running threads.
	 * <p>
	 * Since calling this Method will result in an shutdown of the {@link NetCom2Utils} NetComThread, this will be unusable
	 * in the future.
	 * <p>
	 * This will result in all Queued Runnable of {@link NetCom2Utils#runLater(Runnable)} or {@link NetCom2Utils#runOnNetComThread(Runnable)}
	 * to be shut down forcefully.
	 */
	private void hardStop() {
		running = false;
		logging.info("Shutting threadPool down forcefully! Expect interrupted Exceptions!");
		threadPool.shutdownNow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void launch() throws StartFailedException {
		if (running) {
			logging.error("ServerStart is already launched!");
			throw new StartFailedException("ServerStart is already launched!");
		}
		logging.info("Starting server at port: " + serverConnector.getPort());
		try {
			logging.trace("Initializing connection to port: " + serverConnector.getPort());
			new Initializer(distributor, communicationRegistration, cache, clientList, remoteObjectRegistration).init();
			logging.trace("Establishing Connection ..");
			serverConnector.establishConnection(serverSocketFactory);
			logging.info("Established connection at port: " + serverConnector.getPort());
			running = true;
		} catch (IOException e) {
			logging.fatal("Could not start! Shutting down safely ..", e);
			final StartFailedException startFailedException = new StartFailedException(e);
			try {
				serverConnector.shutDown();
			} catch (IOException e1) {
				logging.error("Encountered Exception, while shutting down Connection!", e1);
				startFailedException.addSuppressed(e1);
			}
			throw startFailedException;
		} catch (StartFailedException startFailedException) {
			throw startFailedException;
		} catch (final Throwable throwable) {
			logging.fatal("Failed to start Server, because of an unexpected Throwable", throwable);
			throw new StartFailedException(throwable);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void acceptAllNextClients() throws ClientConnectionFailedException {
		if(!running()) {
			logging.warn("Server not running!");
			return;
		}
		logging.debug("Starting to accept all next Clients ..");
		try {
			while (running()) {
				acceptNextClient();
			}
			logging.info("SoftStop detected. Disconnecting ...");
		} catch (final ClientConnectionFailedException e) {
			throw new ClientConnectionFailedException(e);
		} catch (final Throwable t) {
			logging.fatal("Caught unexpected Throwable while accepting Clients!", t);
			logging.debug("Trying to at least disconnect ..");
			disconnect();
			throw new ClientConnectionFailedException(t);
		}
		disconnect();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void acceptNextClient() throws ClientConnectionFailedException {
		if (! running) {
			throw new ClientConnectionFailedException("Cannot accept Clients, if not launched!");
		}
		logging.debug("Accepting next Client.");
		final ServerSocket serverSocket = serverConnector.getServerSocket();
		try {
			logging.info("Awaiting new Connection ..");
			final Socket socket = serverSocket.accept();
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPort(final int port) {
		serverConnector = new ServerConnector(port);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPort() {
		return serverConnector.getPort();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addClientConnectedHandler(final ClientConnectedHandler clientConnectedHandler) {
		NetCom2Utils.parameterNotNull(clientConnectedHandler);
		logging.debug("Added ClientConnectedHandler " + clientConnectedHandler);
		synchronized (clientConnectedHandlers) {
			clientConnectedHandlers.add(clientConnectedHandler);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeClientConnectedHandler(final ClientConnectedHandler clientConnectedHandler) {
		NetCom2Utils.parameterNotNull(clientConnectedHandler);
		logging.debug("Removing ClientConnectedHandler " + clientConnectedHandler);
		synchronized (clientConnectedHandlers) {
			clientConnectedHandlers.remove(clientConnectedHandler);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Distributor distribute() {
		return distributor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Cache cache() {
		return cache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect() {
		logging.info("Shutdown requested");
		logging.trace("Performing softStop...");
		softStop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setServerSocketFactory(final Factory<Integer, ServerSocket> factory) {
		NetCom2Utils.parameterNotNull(factory);
		if (serverSocketFactory != null) {
			logging.debug("Overriding existing Factory " + serverSocketFactory + " with " + factory);
		}
		logging.trace("Set ServerSocketFactory to " + factory);
		serverSocketFactory = factory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ClientList clientList() {
		return clientList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final CommunicationRegistration getCommunicationRegistration() {
		return communicationRegistration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setExecutorService(final ExecutorService executorService) {
		NetCom2Utils.parameterNotNull(executorService);
		try {
			threadPoolLock.lock();
			this.threadPool = executorService;
		} finally {
			threadPoolLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RemoteObjectRegistration remoteObjects() {
		return remoteObjectRegistration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void softStop() {
		// This is done, because
		// if the ServerStart is
		// not running, we do not
		// need to go through the
		// disconnect routine
		if(!running()) {
			return;
		}
		logging.debug("Stopping ..");
		logging.trace("Notifying about stop ..");
		running = false;
		logging.trace("Stopping all Clients");
		try {
			clientList.acquire();
			clientList.close();
		} catch (InterruptedException e) {
			logging.catching(e);
		} finally {
			clientList.release();
		}
		logging.trace("Shutting down ThreadPool ..");
		try {
			threadPoolLock.lock();
			threadPool.shutdown();
			try {
				logging.trace("Awaiting termination of all Threads ..");
				threadPool.awaitTermination(20, TimeUnit.SECONDS);
				if (! threadPool.isShutdown()) {
					logging.trace("Detected some running Threads " + 20 + " seconds after ShutdownRequest! Forcefully shutting down the ThreadPool");
					hardStop();
				}
			} catch (InterruptedException e) {
				logging.error("Exception while awaiting termination!", e);
			}
		} finally {
			threadPoolLock.unlock();
		}
		logging.trace("Shutdown request completed!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean running() {
		return running;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLogging(final Logging logging) {
		NetCom2Utils.parameterNotNull(logging);
		this.logging.trace("Updating logging ..");
		this.logging = logging;
		this.logging.debug("Updated logging!");
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public Awaiting createNewConnection(final Session session, final Class key) {
		logging.debug("Trying to create Connection " + key + " for Session " + session);
		logging.trace("Getting Client from ClientList ..");
		final Optional<Client> clientOptional = clientList.getClient(session);
		if (! clientOptional.isPresent()) {
			logging.warn("Could not locate Client for Session: " + session);
			return Synchronize.empty();
		}
		return clientOptional.get().createNewConnection(key);
	}
}
