package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.exceptions.UnknownClientException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientConnectedHandler;
import com.github.thorbenkuck.netcom2.services.ServiceDiscoveryHub;

import java.net.InetSocketAddress;
import java.net.SocketException;

class NativeServerStart implements ServerStart {

	private final Value<InetSocketAddress> addressValue = Value.emptySynchronized();
	private final Value<Logging> loggingValue = Value.synchronize(Logging.unified());
	private final Value<Boolean> running = Value.synchronize(false);
	private final ClientList clientList;
	private final Cache cache;
	private final CommunicationRegistration communicationRegistration;
	private final ClientFactory clientFactory;
	private final Value<ConnectorCore> connectorCoreValue = Value.emptySynchronized();

	NativeServerStart(InetSocketAddress address) {
		Logging logging = loggingValue.get();
		logging.trace("Settings address value");
		this.addressValue.set(address);
		logging.trace("Creating ClientList");
		clientList = ClientList.create();
		logging.trace("Opening and storing Cache");
		cache = Cache.open();
		logging.trace("Opening and storing default CommunicationRegistration");
		communicationRegistration = CommunicationRegistration.open();
		logging.trace("Opening and storing default ClientFactory");
		clientFactory = ClientFactory.open(communicationRegistration);
		logging.trace("Adding default ClientConnectedHandler");
		addClientConnectedHandler(new ClientListConnectedHandler());
		logging.instantiated(this);
	}

	/**
	 * Adds an {@link ClientConnectedHandler}, that should handle a newly created Client.
	 * <p>
	 * Those ClientConnectedHandlers will be asked 2 times. First to create the Client-Object and second to handle this Object.
	 *
	 * @param clientConnectedHandler the Client ConnectedHandler that should be usd
	 */
	@Override
	public void addClientConnectedHandler(ClientConnectedHandler clientConnectedHandler) {
		clientFactory.addClientConnectedHandler(clientConnectedHandler);
	}

	/**
	 * Removes a ClientConnectedHandler from the ServerStart.
	 *
	 * @param clientConnectedHandler the ClientConnectedHandler
	 */
	@Override
	public void removeClientConnectedHandler(ClientConnectedHandler clientConnectedHandler) {
		clientFactory.removeClientConnectedHandler(clientConnectedHandler);
	}

	@Override
	public ClientFactory getClientFactory() {
		return clientFactory;
	}

	@Override
	public synchronized void launch() throws StartFailedException {
		if (running.get()) {
			loggingValue.get().warn("ServerStart is already started! Cannot start an already started NetworkInterface!");
		}
		final Logging logging = loggingValue.get();
		logging.debug("Launching the ServerStart");
		logging.trace("Registering internal Requests ..");
		ServerDefaultCommunication.applyTo(this);
		logging.trace("Requesting connectorCore value ..");
		final InetSocketAddress address = addressValue.get();
		logging.trace("Requesting connection establishment for " + address);
		connectorCoreValue.get().establishConnection(address);

		logging.trace("Updating running flag ..");
		running.set(true);
		logging.info("ServerStart launched at " + getPort());
	}

	@Override
	public void acceptNextClient() throws ClientConnectionFailedException {
		final Logging logging = loggingValue.get();
		logging.debug("Accepting next client.");
		logging.trace("Checking ConnectorCore value ..");
		logging.trace("Requesting next Client handling at ConnectorCore ..");
		connectorCoreValue.get().handleNext();
		logging.debug("New Client handled");
	}

	@Override
	public void acceptAllNextClients() throws ClientConnectionFailedException {
		final Logging logging = loggingValue.get();
		final Thread thread = Thread.currentThread();
		logging.debug("Accepting all connecting Clients on " + thread + ". This Thread will be blocked.");
		while (running()) {
			logging.trace("Requesting acceptance of next Client.");
			acceptNextClient();
			logging.trace("Client acceptance finished.");
		}
		logging.trace("Stop detected. Releasing current Thread." + thread);
	}

	/**
	 * Shuts down the Server and disconnects all connected Clients
	 *
	 * @see #softStop()
	 */
	@Override
	public void disconnect() {
		softStop();
		connectorCoreValue.get().disconnect();
	}

	/**
	 * Returns the internally maintained ClientList.
	 * <p>
	 * This may be used, if you need to get a certain Client or apply something to all Clients.
	 * <p>
	 * If you however want to access certain Clients, it is recommended, to create a custom UserObject and set the UserObject,
	 * aggregating the Session of the Client inside of an custom {@link ClientConnectedHandler}. The Client is an real representation
	 * of the Connected PC. Therefor you can do real, irreversible damage at runtime, resulting in an fatal, unrecoverable
	 * error.
	 *
	 * @return the ClientList
	 */
	@Override
	public ClientList clientList() {
		return clientList;
	}

	/**
	 * Instantiates the creation of the new Connection.
	 * <p>
	 * This call should be Asynchronous, so that the caller may do different things after calling this method.
	 * <p>
	 * For that, an instance of the {@link Awaiting} should be instantiated and returned.
	 * After the Connection is established <b>AND</b> usable, this Awaiting should be continued.
	 *
	 * @param session the Session, for which the new Connection should be used
	 * @param key     the key, which identifies the Connection
	 * @return an instance of the {@link Awaiting} interface for synchronization
	 */
	@Override
	public Awaiting createNewConnection(Session session, Class key) {
		Client client = clientList.getClient(session).orElseThrow(() -> new UnknownClientException("No Client found for " + session));
		return client.createNewConnection(key);
	}

	@Override
	public Cache cache() {
		return cache;
	}

	@Override
	public CommunicationRegistration getCommunicationRegistration() {
		return communicationRegistration;
	}

	/**
	 * Allows to override internally set Logging-instances.
	 * <p>
	 * By default, every component uses the {@link Logging#unified()}, therefore, by calling:
	 * <p>
	 * <code>
	 * Logging instance = ...
	 * NetComLogging.setLogging(instance);
	 * </code>
	 * <p>
	 * you will update the internally used logging mechanisms of all components at the same time.
	 *
	 * @param logging the Logging instance that should be used.
	 */
	@Override
	public void setLogging(Logging logging) {
		loggingValue.set(logging);
	}

	/**
	 * This Method will stop the internal Mechanisms without stopping the thread it is running within.
	 * <p>
	 * The internal Mechanism should therefore depend on the {@link #running()} method. And the {@link #running()} method
	 * should return false, once this method is called.
	 */
	@Override
	public void softStop() {
		running.set(false);
	}

	/**
	 * Defines, whether or not the inheriting class is Running.
	 *
	 * @return true, if {@link #softStop()} was not called yet, else false
	 */
	@Override
	public boolean running() {
		return running.get();
	}

	@Override
	public int getPort() {
		synchronized (addressValue) {
			return addressValue.get().getPort();
		}
	}

	@Override
	public void setPort(int to) {
		if (running.get()) {
			return;
		}
		synchronized (addressValue) {
			InetSocketAddress current = addressValue.get();
			addressValue.set(new InetSocketAddress(current.getAddress(), to));
		}
	}

	@Override
	public ServiceDiscoveryHub allowLocalAreaNetworkFind(int port) throws SocketException {
		ServiceDiscoveryHub hub = ServiceDiscoveryHub.create(port);
		loggingValue.get().info("Instantiated new ServiceDiscoveryHub");
		loggingValue.get().debug("This Instance will not be cached!");
		loggingValue.get().trace("Connecting ServiceDiscoveryHub to this");
		hub.connect(this);
		loggingValue.get().trace("Listening for local area network find-attempts");
		hub.listen();
		return hub;
	}

	@Override
	public void setConnectorCore(ConnectorCore connectorCore) {
		connectorCoreValue.set(connectorCore);
	}

	private final class ClientListConnectedHandler implements ClientConnectedHandler {

		@Override
		public void accept(Client client) {
			loggingValue.get().debug("Storing connected Client into the ClientList");
			clientList.add(client);
		}
	}
}
