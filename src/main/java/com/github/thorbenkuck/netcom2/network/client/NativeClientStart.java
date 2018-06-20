package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.*;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientDisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.DefaultConnection;
import com.github.thorbenkuck.netcom2.network.shared.connections.EventLoop;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public class NativeClientStart implements ClientStart {

	private final Value<SocketAddress> addressValue = Value.emptySynchronized();
	private final Client client;
	private final Value<Boolean> running = Value.synchronize(false);
	private final CommunicationRegistration communicationRegistration;
	private final Cache cache;
	private final Value<Logging> loggingValue = Value.synchronize(Logging.unified());
	private final Synchronize shutdownSynchronize = Synchronize.createDefault();
	private final Value<Thread> parallelBlock = Value.emptySynchronized();
	private final Value<EventLoop> eventLoopValue = Value.emptySynchronized();

	public NativeClientStart(SocketAddress address) {
		this.addressValue.set(address);
		communicationRegistration = CommunicationRegistration.open();
		cache = Cache.open();
		client = Client.create(communicationRegistration);
		loggingValue.get().objectCreated(this);
	}

	/**
	 * Adds a {@link SerializationAdapter} as a fallback serialization instance to this ClientStart.
	 *
	 * @param serializationAdapter the adapter, that should be used.
	 * @see SerializationAdapter
	 */
	@Override
	public void addFallBackSerialization(SerializationAdapter serializationAdapter) {
		client.objectHandler().addFallbackSerialization(serializationAdapter);
	}

	/**
	 * Adds a {@link DeSerializationAdapter} as a fallback deserialization instance to this ClientStart.
	 *
	 * @param deSerializationAdapter the adapter, that should be used.
	 * @see DeSerializationAdapter
	 */
	@Override
	public void addFallBackDeSerialization(DeSerializationAdapter deSerializationAdapter) {
		client.objectHandler().addFallbackDeserialization(deSerializationAdapter);
	}

	/**
	 * Sets the {@link SerializationAdapter} as the main serialization instance to this ClientStart.
	 * <p>
	 * This instance will be asked first, before the fallback instances will be asked
	 *
	 * @param mainSerializationAdapter the adapter, that should be used.
	 * @see SerializationAdapter
	 */
	@Override
	public void setMainSerializationAdapter(SerializationAdapter mainSerializationAdapter) {
		client.objectHandler().setMainSerialization(mainSerializationAdapter);
	}

	/**
	 * Sets the {@link DeSerializationAdapter} as the main deserialization instance to this ClientStart.
	 * <p>
	 * This instance will be asked first, before the fallback instances will be asked
	 *
	 * @param mainDeSerializationAdapter the adapter, that should be used.
	 * @see DeSerializationAdapter
	 */
	@Override
	public void setMainDeSerializationAdapter(DeSerializationAdapter mainDeSerializationAdapter) {
		client.objectHandler().setMainDeserialization(mainDeSerializationAdapter);
	}

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
	@Override
	public void addDisconnectedHandler(ClientDisconnectedHandler clientDisconnectedHandler) {
		client.addDisconnectedHandler(clientDisconnectedHandler);
	}

	@Override
	public void addDecryptionAdapter(DecryptionAdapter decryptionAdapter) {
		client.objectHandler().addDecryptionAdapter(decryptionAdapter);
	}

	/**
	 * Sets an Adapter for encryption of Strings that should be send.
	 *
	 * @param encryptionAdapter the EncryptionAdapter
	 * @see EncryptionAdapter
	 */
	@Override
	public void addEncryptionAdapter(EncryptionAdapter encryptionAdapter) {
		client.objectHandler().addEncryptionAdapter(encryptionAdapter);
	}

	/**
	 * This Method is a shortcut for: {@link Cache#reset()}
	 *
	 * @see Cache#reset()
	 */
	@Override
	public void clearCache() {
		cache.reset();
	}

	@Override
	public Cache cache() {
		return cache;
	}

	@Override
	public CommunicationRegistration getCommunicationRegistration() {
		return communicationRegistration;
	}

	@Override
	public void launch() throws StartFailedException {
		final EventLoop eventLoop;
		try {
			eventLoop = EventLoop.openNIO();
			eventLoop.start();
		} catch (IOException e) {
			throw new StartFailedException(e);
		}

		client.setSession(Session.open(client));

		eventLoopValue.set(eventLoop);
		final SocketChannel socketChannel;
		try {
			socketChannel = SocketChannel.open(addressValue.get());
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			throw new StartFailedException(e);
		}

		Connection connection = Connection.nio(socketChannel);
		connection.setIdentifier(DefaultConnection.class);
		connection.hook(client);

		try {
			eventLoop.register(connection);
		} catch (IOException e) {
			throw new StartFailedException(e);
		}

		running.set(false);
	}

	private void createBlockingThread() {
		synchronized (parallelBlock) {
			if (!parallelBlock.isEmpty()) {
				loggingValue.get().warn("Only one block till finished call is allowed!");
				return;
			}
			final Thread thread = new Thread(this::blockOnCurrentThread);
			thread.setDaemon(false);
			thread.setName("NetCom2-Blocking-Thread");
			parallelBlock.set(thread);
		}
	}

	@Override
	public void startBlockerThread() {
		createBlockingThread();
		final Thread thread = parallelBlock.get();
		thread.start();
	}

	@Override
	public void blockOnCurrentThread() {
		while (running()) {
			try {
				shutdownSynchronize.synchronize();
			} catch (InterruptedException e) {
				loggingValue.get().catching(e);
			}
		}
	}

	/**
	 * Allows to override internally set Logging-instances.
	 * <p>
	 * By default, every component uses the {@link Logging#unified()}, therefore, by calling:
	 * <p>
	 * <code>
	 * Logging instance = ...
	 * NetComLogging.setLogging(instance);
	 * </code>
	 * <p>
	 * you will update the internally used logging mechanisms of all components at the same time.
	 *
	 * @param logging the Logging instance that should be used.
	 */
	@Override
	public void setLogging(Logging logging) {

	}

	/**
	 * This Method will stop the internal Mechanisms without stopping the thread it is running within.
	 * <p>
	 * The internal Mechanism should therefore depend on the {@link #running()} method. And the {@link #running()} method
	 * should return false, once this method is called.
	 */
	@Override
	public void softStop() {
		running.set(false);
	}

	/**
	 * Defines, whether or not the inheriting class is Running.
	 *
	 * @return true, if {@link #softStop()} was not called yet, else false
	 */
	@Override
	public boolean running() {
		return running.get();
	}

	@APILevel
	Client getClient() {
		return client;
	}
}
