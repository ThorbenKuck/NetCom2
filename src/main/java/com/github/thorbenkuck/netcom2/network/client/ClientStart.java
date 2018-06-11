package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.interfaces.NetworkInterface;
import com.github.thorbenkuck.netcom2.interfaces.SoftStoppable;
import com.github.thorbenkuck.netcom2.network.shared.DeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.shared.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.shared.SerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.client.ClientDisconnectedHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface ClientStart extends RemoteObjectAccess, NetworkInterface, SoftStoppable {

	static ClientStart at(String hostname, int port) {
		return at(new InetSocketAddress(hostname, port));
	}

	static ClientStart at(SocketAddress socketAddress) {
		return new NativeClientStart(socketAddress);
	}

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
	void addFallBackSerialization(final SerializationAdapter serializationAdapter);

	/**
	 * Adds a {@link DeSerializationAdapter} as a fallback deserialization instance to this ClientStart.
	 *
	 * @param deSerializationAdapter the adapter, that should be used.
	 * @see DeSerializationAdapter
	 */
	void addFallBackDeSerialization(final DeSerializationAdapter deSerializationAdapter);

	/**
	 * Sets the {@link SerializationAdapter} as the main serialization instance to this ClientStart.
	 * <p>
	 * This instance will be asked first, before the fallback instances will be asked
	 *
	 * @param mainSerializationAdapter the adapter, that should be used.
	 * @see SerializationAdapter
	 */
	void setMainSerializationAdapter(final SerializationAdapter mainSerializationAdapter);

	/**
	 * Sets the {@link DeSerializationAdapter} as the main deserialization instance to this ClientStart.
	 * <p>
	 * This instance will be asked first, before the fallback instances will be asked
	 *
	 * @param mainDeSerializationAdapter the adapter, that should be used.
	 * @see DeSerializationAdapter
	 */
	void setMainDeSerializationAdapter(final DeSerializationAdapter mainDeSerializationAdapter);

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
	 * @param clientDisconnectedHandler the Handler, that should be called once the Connection is terminated
	 */
	void addDisconnectedHandler(final ClientDisconnectedHandler clientDisconnectedHandler);

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
}
