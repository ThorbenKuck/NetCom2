package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.client.ClientStartImpl;
import com.github.thorbenkuck.netcom2.network.client.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.client.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.DeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.clients.SerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

public interface ClientStart extends Launch, Loggable {

	static ClientStart at(String address, int port) {
		return new ClientStartImpl(address, port);
	}

	Cache cache();

	Awaiting createNewConnection(Class key);

	void setSocketFactory(SocketFactory factory);

	Sender send();

	void addFallBackSerialization(SerializationAdapter<Object, String> serializationAdapter);

	void addFallBackDeSerialization(DeSerializationAdapter<String, Object> deSerializationAdapter);

	void setMainSerializationAdapter(SerializationAdapter<Object, String> mainSerializationAdapter);

	void setMainDeSerializationAdapter(DeSerializationAdapter<String, Object> mainDeSerializationAdapter);

	void addDisconnectedHandler(DisconnectedHandler disconnectedHandler);

	void setDecryptionAdapter(DecryptionAdapter decryptionAdapter);

	void setEncryptionAdapter(EncryptionAdapter encryptionAdapter);

	CommunicationRegistration getCommunicationRegistration();

	void clearCache();
}
