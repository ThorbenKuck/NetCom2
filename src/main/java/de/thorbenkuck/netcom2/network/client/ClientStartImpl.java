package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.interfaces.SocketFactory;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.ClientStart;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.clients.DeSerializationAdapter;
import de.thorbenkuck.netcom2.network.shared.clients.SerializationAdapter;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

import java.io.IOException;
import java.net.Socket;

public class ClientStartImpl implements ClientStart {

	private final Cache cache = Cache.create();
	private final ClientConnector clientConnector;
	private final CommunicationRegistration communicationRegistration = CommunicationRegistration.create();
	private final Logging logging = new NetComLogging();
	private SocketFactory socketFactory;
	private Client client;
	private InternalSender sender;

	public ClientStartImpl(String address, int port) {
		clientConnector = new ClientConnector(address, port);
		setSocketFactory((port1, address1) -> {
			try {
				return new Socket(address1, port1);
			} catch (IOException e) {
				throw new Error(e);
			}
		});
	}

	@Override
	public void launch() throws StartFailedException {
		logging.debug("Connecting ..");
		try {
			clientConnector.establishConnection(socketFactory);
		} catch (IOException e) {
			throw new StartFailedException(e);
		}
		logging.trace("Creating required elements ..");
		client = new Client(clientConnector.getSocket(), communicationRegistration);
		try {
			client.invoke();
		} catch (IOException e) {
			throw new StartFailedException(e);
		}
		sender = InternalSender.create(client, cache);
		new Initializer(client, communicationRegistration, cache, sender).init();
		logging.info("Connected to server at " + clientConnector.getSocket().toString());
	}

	@Override
	public void setLogging(Logging logging) {
		NetComLogging.setLogging(logging);
	}

	@Override
	public void registerTo(Class clazz) {
		logging.error("Not implemented yet!");
	}

	@Override
	public void setSocketFactory(SocketFactory factory) {
		socketFactory = factory;
	}

	@Override
	public Sender send() {
		return sender;
	}

	@Override
	public void addFallBackSerialization(SerializationAdapter<Object, String> serializationAdapter) {
		client.addFallBackSerialization(serializationAdapter);
	}

	@Override
	public void addFallBackDeSerialization(DeSerializationAdapter<String, Object> deSerializationAdapter) {
		client.addFallBackDeSerialization(deSerializationAdapter);
	}

	@Override
	public void setMainSerializationAdapter(SerializationAdapter<Object, String> mainSerializationAdapter) {
		client.setMainSerializationAdapter(mainSerializationAdapter);
	}

	@Override
	public void setMainDeSerializationAdapter(DeSerializationAdapter<String, Object> mainDeSerializationAdapter) {
		client.setMainDeSerializationAdapter(mainDeSerializationAdapter);
	}

	@Override
	public void addDisconnectedHandler(DisconnectedHandler disconnectedHandler) {
		client.addDisconnectedHandler(disconnectedHandler);
	}

	@Override
	public CommunicationRegistration getCommunicationRegistration() {
		return communicationRegistration;
	}

	@Override
	public String toString() {
		return "ClientStart{" +
				"cache=" + cache +
				", socketFactory=" + socketFactory +
				", communicationRegistration=" + communicationRegistration +
				", client=" + client +
				'}';
	}
}
