package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.annotations.Synchronized;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.interfaces.SocketFactory;
import de.thorbenkuck.netcom2.network.interfaces.ClientStart;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.clients.*;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

import java.io.IOException;

@Synchronized
public class ClientStartImpl implements ClientStart {

	private final Cache cache = Cache.create();
	private final ClientConnector clientConnector;
	private final CommunicationRegistration communicationRegistration = CommunicationRegistration.create();
	private final ClientConnectionEstablish clientConnectionEstablish = new ClientConnectionEstablish();
	private Logging logging = Logging.unified();
	private SocketFactory socketFactory;
	private Client client;
	private InternalSender sender;
	boolean launched;

	public ClientStartImpl(String address, int port) {
		logging.debug("Instantiation ClientStart ..");
		logging.trace("Creating Client ..");
		client = Client.create(communicationRegistration);
		logging.trace("Creating Client-Connector ..");
		clientConnector = new ClientConnector(address, port, client);
		logging.trace("Setting DefaultClientSocketFactory ..");
		setSocketFactory(new DefaultClientSocketFactory());
		logging.trace("Creating Sender ..");
		sender = InternalSender.create(client, cache);
		client.addDisconnectedHandler(new DefaultClientDisconnectedHandler(this));
	}

	@Override
	public synchronized void launch() throws StartFailedException {
		if(launched) {
			logging.warn("Requested launch cannot be performed, already launched!");
			return;
		}
		logging.debug("Connecting to server ..");
		synchronized (clientConnector) {
			try {
				logging.trace("Trying to establish Connection ..");
				clientConnector.establishConnection(socketFactory);
			} catch (IOException e) {
				throw new StartFailedException(e);
			}
			logging.trace("Initializing new Connection ..");
			new Initializer(client, communicationRegistration, cache, sender, clientConnector, socketFactory).init();
			launched = true;
		}
		logging.info("Connected to server at " + client.getConnection(DefaultConnection.class));
	}

	@Override
	public Cache cache() {
		return cache;
	}

	@Override
	public Awaiting createNewConnection(Class key) {
		logging.trace("Trying to establish new Connection ..");
		return clientConnectionEstablish.newFor(key, client);
	}

	@Override
	public synchronized void setSocketFactory(SocketFactory factory) {
		logging.debug("Set SocketFactory to: " + factory);
		socketFactory = factory;
	}

	@Override
	public Sender send() {
		return sender;
	}

	@Override
	public synchronized void addFallBackSerialization(SerializationAdapter<Object, String> serializationAdapter) {
		logging.debug("Added fallback Serialization " + serializationAdapter);
		client.addFallBackSerialization(serializationAdapter);
	}

	@Override
	public synchronized void addFallBackDeSerialization(DeSerializationAdapter<String, Object> deSerializationAdapter) {
		logging.debug("Added fallback Serialization " + deSerializationAdapter);
		client.addFallBackDeSerialization(deSerializationAdapter);
	}

	@Override
	public synchronized void setMainSerializationAdapter(SerializationAdapter<Object, String> mainSerializationAdapter) {
		logging.debug("Set main Serialization " + mainSerializationAdapter);
		client.setMainSerializationAdapter(mainSerializationAdapter);
	}

	@Override
	public synchronized void setMainDeSerializationAdapter(DeSerializationAdapter<String, Object> mainDeSerializationAdapter) {
		logging.debug("Added main Serialization " + mainDeSerializationAdapter);
		client.setMainDeSerializationAdapter(mainDeSerializationAdapter);
	}

	@Override
	public synchronized void addDisconnectedHandler(DisconnectedHandler disconnectedHandler) {
		logging.debug("Added disconnectedHandler " + disconnectedHandler);
		client.addDisconnectedHandler(disconnectedHandler);
	}

	@Override
	public synchronized void setDecryptionAdapter(DecryptionAdapter decryptionAdapter) {
		logging.debug("Set DecryptionAdapter " + decryptionAdapter);
		client.setDecryptionAdapter(decryptionAdapter);
	}

	@Override
	public synchronized void setEncryptionAdapter(EncryptionAdapter encryptionAdapter) {
		logging.debug("Set EncryptionAdapter " + encryptionAdapter);
		client.setEncryptionAdapter(encryptionAdapter);
	}

	@Override
	public CommunicationRegistration getCommunicationRegistration() {
		return communicationRegistration;
	}

	@Override
	public synchronized void clearCache() {
		logging.debug("Clearing cache ..");
		cache.reset();
	}

	@Override
	public synchronized void setLogging(Logging logging) {
		this.logging.debug("Overriding logging ..");
		this.logging = logging;
		logging.debug("Logging was updated!");
	}

	@Override
	public String toString() {
		return "ClientStart{" +
				"cache=" + cache +
				", socketFactory=" + socketFactory +
				", communicationRegistration=" + communicationRegistration +
				", clientImpl=" + client +
				'}';
	}

	void runSynchronized(Runnable runnable) {
		synchronized (clientConnector) {
			runnable.run();
		}
	}
}
