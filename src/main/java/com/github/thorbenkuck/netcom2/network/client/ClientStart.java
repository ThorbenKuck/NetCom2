package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.interfaces.RemoteObjectAccess;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.interfaces.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.Launch;
import com.github.thorbenkuck.netcom2.network.interfaces.Loggable;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.DeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.clients.SerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

/*
&lt; for < and &gt; for > .
 */

/**
 * This Class is the used to connect to an launched {@link com.github.thorbenkuck.netcom2.network.server.ServerStart}.
 * <p>
 * It is the main entry-point for any Client-related actions. It further never exposes its implementation. You may initialize
 * a Client the following way:
 * <p>
 * <code>
 * final String address = "localhost"; // the address of the Server
 * final int port = 4444; // the port of the Server
 * final ClientStart clientStart = ClientStart.at(address, port);
 * </code>
 * <p>
 * Note however, that the Server has to:
 * <ol type="a">
 * <li>Be already running, if you call <code>clientStart.launch()</code></li>
 * <li>Be assigned to the same port and address as given to the ClientStart</li>
 * </ol>
 * <p>
 * The following code starts a Server and a Client, which both successfully connect:
 * <p>
 * <code>
 * ServerStart serverStart = ServerStart.at(4444);
 * ClientStart clientStart = ClientStart.at("localhost", 4444);
 * <p>
 * try {
 * serverStart.launch();
 * } catch(Exception ignored) {}
 * new Thread(() -&gt; {
 * try {
 * serverStart.acceptAllNextClients();
 * } catch(ClientConnectionFailedException ignored) {}
 * }).start();
 * <p>
 * try {
 * clientStart.launch();
 * } catch(Exception ignored) {}
 * </code>
 * <p>
 * Once the launch method of the ServerStart is finished, the ClientStart might be launched.
 * If however, the ServerStart is not yet launched, the ClientStart.launch method will fail and throw
 * an StartFailedException.
 *
 * @see com.github.thorbenkuck.netcom2.network.server.ServerStart
 */
public interface ClientStart extends Launch, Loggable, RemoteObjectAccess {

	/**
	 * Creates a new ClientStart.
	 * <p>
	 * The use of this method is recommended to be used! At all times, this method is ensured to never change.
	 * Therefor, relying on any implementation details, is not recommended, because they might become subject to change.
	 *
	 * @param address the address of the already running ServerStart as a String
	 * @param port    the port, that the ServerStart is bound to.
	 * @return a new Instance of this interface.
	 */
	static ClientStart at(final String address, final int port) {
		return new ClientStartImpl(address, port);
	}

	/**
	 * Provides the internal cache of the ClientStart.
	 * <p>
	 * The Cache is used, to hold registered Objects and may be updated manually.
	 * Also, you may add manual observers.
	 *
	 * @return an instance of the Cache
	 * @see Sender
	 */
	Cache cache();

	/**
	 * This Method-Call will create a new Connection, identified by the provided <code>key</code>.
	 * <p>
	 * Calling this Method, will lead to a request-response-chain between the Server and the Client.
	 * After this, the Connection will be created an is usable. This whole procedure is asynchronous. To allow synchronization,
	 * you may use the returned {@link Awaiting} instance
	 *
	 * @param key the identifying Class for the new Connection
	 * @return an synchronization mechanism
	 * @see com.github.thorbenkuck.netcom2.network.shared.clients.AbstractConnection
	 * @see com.github.thorbenkuck.netcom2.network.shared.clients.Connection
	 */
	Awaiting createNewConnection(final Class key);

	/**
	 * Sets the Factory, to create the Socket, used by the ClientStart
	 *
	 * @param factory the factory, that creates the Socket
	 * @see SocketFactory
	 */
	void setSocketFactory(final SocketFactory factory);

	/**
	 * Used to send Objects to the ServerStart.
	 *
	 * @return an instance of the {@link Sender} interface
	 * @see Sender
	 */
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

	RemoteObjectFactory getRemoteObjectFactory();
}
