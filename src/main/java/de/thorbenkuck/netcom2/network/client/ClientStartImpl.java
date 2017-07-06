package de.thorbenkuck.netcom2.network.client;

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

public class ClientStartImpl implements ClientStart {

	private final Cache cache = Cache.create();
	private final ClientConnector clientConnector;
	private final CommunicationRegistration communicationRegistration = CommunicationRegistration.create();
	private final ClientConnectionEstablish clientConnectionEstablish = new ClientConnectionEstablish();
	private Logging logging = Logging.unified();
	private SocketFactory socketFactory;
	private Client client;
	private InternalSender sender;

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
	public void launch() throws StartFailedException {
		logging.debug("Connecting to server ..");
		try {
			logging.trace("Trying to establish Connection ..");
			clientConnector.establishConnection(socketFactory);
		} catch (IOException e) {
			throw new StartFailedException(e);
		}
		logging.trace("Initializing new Connection ..");
		new Initializer(client, communicationRegistration, cache, sender, clientConnector, socketFactory).init();
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
	public void setSocketFactory(SocketFactory factory) {
		logging.debug("Set SocketFactory to: " + factory);
		socketFactory = factory;
	}

	@Override
	public Sender send() {
		return sender;
	}

	@Override
	public void addFallBackSerialization(SerializationAdapter<Object, String> serializationAdapter) {
		logging.debug("Added fallback Serialization " + serializationAdapter);
		client.addFallBackSerialization(serializationAdapter);
	}

	@Override
	public void addFallBackDeSerialization(DeSerializationAdapter<String, Object> deSerializationAdapter) {
		logging.debug("Added fallback Serialization " + deSerializationAdapter);
		client.addFallBackDeSerialization(deSerializationAdapter);
	}

	@Override
	public void setMainSerializationAdapter(SerializationAdapter<Object, String> mainSerializationAdapter) {
		logging.debug("Set main Serialization " + mainSerializationAdapter);
		client.setMainSerializationAdapter(mainSerializationAdapter);
	}

	@Override
	public void setMainDeSerializationAdapter(DeSerializationAdapter<String, Object> mainDeSerializationAdapter) {
		logging.debug("Added main Serialization " + mainDeSerializationAdapter);
		client.setMainDeSerializationAdapter(mainDeSerializationAdapter);
	}

	@Override
	public void addDisconnectedHandler(DisconnectedHandler disconnectedHandler) {
		logging.debug("Added disconnectedHandler " + disconnectedHandler);
		client.addDisconnectedHandler(disconnectedHandler);
	}

	@Override
	public void setDecryptionAdapter(DecryptionAdapter decryptionAdapter) {
		logging.debug("Set DecryptionAdapter " + decryptionAdapter);
		client.setDecryptionAdapter(decryptionAdapter);
	}

	@Override
	public void setEncryptionAdapter(EncryptionAdapter encryptionAdapter) {
		logging.debug("Set EncryptionAdapter " + encryptionAdapter);
		client.setEncryptionAdapter(encryptionAdapter);
	}

	@Override
	public CommunicationRegistration getCommunicationRegistration() {
		return communicationRegistration;
	}

	@Override
	public void clearCache() {
		logging.debug("Clearing cache observers ..");
		cache.clearObservers();
	}

	@Override
	public void setLogging(Logging logging) {
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
}
