package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.interfaces.RemoteObjectAccess;
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

public interface ClientStart extends Launch, Loggable, RemoteObjectAccess {

	static ClientStart at(final String address, final int port) {
		return new ClientStartImpl(address, port);
	}

	Cache cache();

	Awaiting createNewConnection(final Class key);

	void setSocketFactory(final SocketFactory factory);

	Sender send();

	void addFallBackSerialization(final SerializationAdapter<Object, String> serializationAdapter);

	void addFallBackDeSerialization(final DeSerializationAdapter<String, Object> deSerializationAdapter);

	void setMainSerializationAdapter(final SerializationAdapter<Object, String> mainSerializationAdapter);

	void setMainDeSerializationAdapter(final DeSerializationAdapter<String, Object> mainDeSerializationAdapter);

	void addDisconnectedHandler(final DisconnectedHandler disconnectedHandler);

	void setDecryptionAdapter(final DecryptionAdapter decryptionAdapter);

	void setEncryptionAdapter(final EncryptionAdapter encryptionAdapter);

	CommunicationRegistration getCommunicationRegistration();

	void clearCache();
}
