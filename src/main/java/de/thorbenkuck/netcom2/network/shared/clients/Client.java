package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.network.client.DecryptionAdapter;
import de.thorbenkuck.netcom2.network.client.EncryptionAdapter;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import de.thorbenkuck.netcom2.network.shared.Expectable;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Client {

	static Client create(CommunicationRegistration communicationRegistration) {
		return new ClientImpl(communicationRegistration);
	}

	void setFallBackDeSerializationAdapter(List<DeSerializationAdapter<String, Object>> fallBackDeSerializationAdapter);

	void setup();

	void disconnect();

	void triggerPrimation();

	Awaiting primed();

	void newPrimation();

	Session getSession();

	void setSession(Session session);

	void clearSession();

	void addFallBackSerialization(SerializationAdapter<Object, String> serializationAdapter);

	void addFallBackDeSerialization(DeSerializationAdapter<String, Object> deSerializationAdapter);

	void addDisconnectedHandler(DisconnectedHandler disconnectedHandler);

	Awaiting createNewConnection(Class connectionKey);

	Expectable send(Object object);

	Expectable send(Class connectionKey, Object object);

	Expectable send(Connection connection, Object object);

	Optional<Connection> getConnection(Class connectionKey);

	ClientID getID();

	void setID(ClientID id);

	void setConnection(Class key, Connection connection);

	CommunicationRegistration getCommunicationRegistration();

	DeSerializationAdapter<String, Object> getMainDeSerializationAdapter();

	void setMainDeSerializationAdapter(DeSerializationAdapter<String, Object> mainDeSerializationAdapter);

	Set<DeSerializationAdapter<String, Object>> getFallBackDeSerialization();

	DecryptionAdapter getDecryptionAdapter();

	void setDecryptionAdapter(DecryptionAdapter decryptionAdapter);

	SerializationAdapter<Object, String> getMainSerializationAdapter();

	void setMainSerializationAdapter(SerializationAdapter<Object, String> mainSerializationAdapter);

	Set<SerializationAdapter<Object, String>> getFallBackSerialization();

	EncryptionAdapter getEncryptionAdapter();

	void setEncryptionAdapter(EncryptionAdapter encryptionAdapter);

	Awaiting prepareConnection(Class clazz);

	boolean isConnectionPrepared(Class clazz);

	void notifyAboutPreparedConnection(Class clazz);

	void addFalseID(ClientID clientID);

	List<ClientID> getFalseIDs();

	void removeFalseID(ClientID clientID);

	void removeFalseIDs(List<ClientID> clientIDS);
}
