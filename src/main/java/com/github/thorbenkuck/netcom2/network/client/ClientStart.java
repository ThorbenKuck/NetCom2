package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.interfaces.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.NetworkInterface;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.DeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.clients.SerializationAdapter;

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
 * try {
 * serverStart.launch();
 * } catch(Exception ignored) {}
 * new Thread(() -&gt; {
 * try {
 * serverStart.acceptAllNextClients();
 * } catch(ClientConnectionFailedException ignored) {}
 * }).start();
 * try {
 * clientStart.launch();
 * } catch(Exception ignored) {}
 * </code>
 * <p>
 * Once the launch method of the ServerStart is finished, the ClientStart might be launched.
 * If however, the ServerStart is not yet launched, the ClientStart.launch method will fail and throw
 * an StartFailedException.
 *
 * @version 1.1
 * @see com.github.thorbenkuck.netcom2.network.server.ServerStart
 * @since 1.0
 */
public interface ClientStart extends RemoteObjectAccess, NetworkInterface {

	/**
	 * Creates a new ClientStart.
	 * <p>
	 * The use of this method is recommended to be used! At all times, this method is ensured to never change.
	 * Therefore, relying on any implementation details, is not recommended, because they might become subject to change.
	 *
	 * @param address the address of the already running ServerStart as a String
	 * @param port    the port, that the ServerStart is bound to.
	 * @return a new Instance of this interface.
	 */
	static ClientStart at(final String address, final int port) {
		return new ClientStartImpl(address, port);
	}

	/**
	 * This Method-Call will access a new Connection, identified by the provided <code>key</code>.
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
	 * Sets the Factory, to access the Socket, used by the ClientStart
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

	/**
	 * Adds a {@link SerializationAdapter} as a fallback serialization instance to this ClientStart.
	 *
	 * @param serializationAdapter the adapter, that should be used.
	 * @see SerializationAdapter
	 */
	void addFallBackSerialization(final SerializationAdapter<Object, String> serializationAdapter);

	/**
	 * Adds a {@link DeSerializationAdapter} as a fallback deserialization instance to this ClientStart.
	 *
	 * @param deSerializationAdapter the adapter, that should be used.
	 * @see DeSerializationAdapter
	 */
	void addFallBackDeSerialization(final DeSerializationAdapter<String, Object> deSerializationAdapter);

	/**
	 * Sets the {@link SerializationAdapter} as the main serialization instance to this ClientStart.
	 * <p>
	 * This instance will be asked first, before the fallback instances will be asked
	 *
	 * @param mainSerializationAdapter the adapter, that should be used.
	 * @see SerializationAdapter
	 */
	void setMainSerializationAdapter(final SerializationAdapter<Object, String> mainSerializationAdapter);

	/**
	 * Sets the {@link DeSerializationAdapter} as the main deserialization instance to this ClientStart.
	 * <p>
	 * This instance will be asked first, before the fallback instances will be asked
	 *
	 * @param mainDeSerializationAdapter the adapter, that should be used.
	 * @see DeSerializationAdapter
	 */
	void setMainDeSerializationAdapter(final DeSerializationAdapter<String, Object> mainDeSerializationAdapter);

	/**
	 * Adds a Handler, that will be invoked once the Connection between the Server and the Client is terminated.
	 * <p>
	 * Any Handler set, will be completely invoked if:
	 * <p>
	 * <ul>
	 * <li>The Server calls {@link com.github.thorbenkuck.netcom2.network.shared.clients.Client#disconnect()}.</li>
	 * <li>The internet-connection between the ServerStart and the ClientStart breaks.</li>
	 * <li>Some IO-Exception is encountered within all Sockets of a active Connections</li>
	 * </ul>
	 *
	 * @param disconnectedHandler the Handler, that should be called once the Connection is terminated
	 */
	void addDisconnectedHandler(final DisconnectedHandler disconnectedHandler);

	/**
	 * Sets an Adapter for decryption of received Strings.
	 *
	 * @param decryptionAdapter the DecryptionAdapter
	 * @see DecryptionAdapter
	 */
	void setDecryptionAdapter(final DecryptionAdapter decryptionAdapter);

	/**
	 * Sets an Adapter for encryption of Strings that should be send.
	 *
	 * @param encryptionAdapter the EncryptionAdapter
	 * @see EncryptionAdapter
	 */
	void setEncryptionAdapter(final EncryptionAdapter encryptionAdapter);

	/**
	 * This Method is a shortcut for: {@link Cache#reset()}
	 *
	 * @see Cache#reset()
	 */
	void clearCache();

	/**
	 * Returns the internally maintained {@link RemoteObjectFactory}.
	 * <p>
	 * This method will never return null.
	 *
	 * @return the internally maintained instance of a RemoteObjectFactory.
	 */
	RemoteObjectFactory getRemoteObjectFactory();
}
