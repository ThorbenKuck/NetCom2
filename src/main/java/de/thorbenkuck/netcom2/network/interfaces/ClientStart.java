package de.thorbenkuck.netcom2.network.interfaces;

import de.thorbenkuck.netcom2.interfaces.SocketFactory;
import de.thorbenkuck.netcom2.network.client.ClientStartImpl;
import de.thorbenkuck.netcom2.network.client.Sender;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import de.thorbenkuck.netcom2.network.shared.clients.DeSerializationAdapter;
import de.thorbenkuck.netcom2.network.shared.clients.SerializationAdapter;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

public interface ClientStart extends Launch, Loggable {

	static ClientStart at(String address, int port) {
		return new ClientStartImpl(address, port);
	}

	Awaiting createNewConnection(Class key) throws InterruptedException;

	void setSocketFactory(SocketFactory factory);

	Sender send();

	void addFallBackSerialization(SerializationAdapter<Object, String> serializationAdapter);

	void addFallBackDeSerialization(DeSerializationAdapter<String, Object> deSerializationAdapter);

	void setMainSerializationAdapter(SerializationAdapter<Object, String> mainSerializationAdapter);

	void setMainDeSerializationAdapter(DeSerializationAdapter<String, Object> mainDeSerializationAdapter);

	void addDisconnectedHandler(DisconnectedHandler disconnectedHandler);

	CommunicationRegistration getCommunicationRegistration();

	void clearCache();
}
