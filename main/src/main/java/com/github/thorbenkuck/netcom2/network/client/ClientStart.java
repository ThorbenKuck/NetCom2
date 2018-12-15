package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.annotations.Experimental;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.netcom2.exceptions.ConnectionEstablishmentFailedException;
import com.github.thorbenkuck.netcom2.interfaces.NetworkInterface;
import com.github.thorbenkuck.netcom2.interfaces.SoftStoppable;
import com.github.thorbenkuck.netcom2.network.shared.DeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.shared.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.shared.SerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientDisconnectedHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface ClientStart extends NetworkInterface, SoftStoppable {

	static ClientStart at(String hostname, int port) {
		return at(new InetSocketAddress(hostname, port));
	}

	static ClientStart at(SocketAddress socketAddress) {
		return nio(socketAddress);
	}

	static ClientStart nio(String hostname, int port) {
		return nio(new InetSocketAddress(hostname, port));
	}

	static ClientStart nio(SocketAddress socketAddress) {
		return as(socketAddress, ClientCore.nio());
	}

	static ClientStart tcp(String hostname, int port) {
		return tcp(new InetSocketAddress(hostname, port));
	}

	static ClientStart tcp(SocketAddress socketAddress) {
		return as(socketAddress, ClientCore.tcp());
	}

	@Experimental
	static ClientStart udp(String hostname, int port) {
		return udp(new InetSocketAddress(hostname, port));
	}

	@Experimental
	static ClientStart udp(SocketAddress socketAddress) {
		return as(socketAddress, ClientCore.udp());
	}

	static ClientStart as(SocketAddress socketAddress, ClientCore clientCore) {
		return new NativeClientStart(socketAddress, clientCore);
	}

	Awaiting newConnection(Class<?> identifier) throws ConnectionEstablishmentFailedException;

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

	void addDecryptionAdapter(DecryptionAdapter decryptionAdapter);

	void addEncryptionAdapter(EncryptionAdapter encryptionAdapter);

	/**
	 * This Method is a shortcut for: {@link Cache#reset()}
	 *
	 * @see Cache#reset()
	 */
	void clearCache();

	/**
	 * Calling this Method is used to block the application from existing.
	 * <p>
	 * By Default, NetCom2 utilizes mostly daemon Threads. This means, you could prepare a ClientStart and launch it,
	 * but the program exists right after the last statement.
	 * <p>
	 * To counter this, the ClientStart provides 2 functions, this and the function {@link #blockOnCurrentThread()}.
	 * <p>
	 * This Methods starts a new non-daemon Thread. This Thread will run, until an internal Synchronization mechanism is
	 * finished.
	 * <p>
	 * This Thread may not be joined or interrupted, nor shut down forcefully.
	 * <p>
	 * You can shut it down, using {@link #softStop()}.
	 * <p>
	 * The Thread utilizes the method {@link #blockOnCurrentThread()} and allows you to use the current Thread for different
	 * Tasks. On the other hand does this Method create a new Thread.
	 *
	 * @see #blockOnCurrentThread()
	 * @see #softStop()
	 */
	void startBlockerThread();

	/**
	 * Calling this Method is used to block the application from existing.
	 * <p>
	 * By Default, NetCom2 utilizes mostly daemon Threads. This means, you could prepare a ClientStart and launch it,
	 * but the program exists right after the last statement.
	 * <p>
	 * To counter this, the ClientStart provides 2 functions, this and the function {@link #startBlockerThread()}.
	 * <p>
	 * This Method synchronizes over an internally maintained {@link com.github.thorbenkuck.keller.sync.Synchronize} instance.
	 * This means, calling this Method will result in a block of the current Thread until {@link #softStop()} is called
	 * and all dependencies are shut down.
	 * <p>
	 * The absolute shutdown time is not known, but you may use this Method freely to synchronize your application.
	 *
	 * @see #startBlockerThread()
	 * @see #softStop()
	 */
	void blockOnCurrentThread();
}
