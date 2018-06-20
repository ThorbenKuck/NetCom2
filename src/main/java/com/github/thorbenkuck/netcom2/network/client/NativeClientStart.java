package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.*;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientDisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

import java.net.SocketAddress;

class NativeClientStart implements ClientStart {

	private final Value<SocketAddress> addressValue = Value.emptySynchronized();
	private final Client client;
	private final Value<Boolean> running = Value.synchronize(false);
	private final CommunicationRegistration communicationRegistration;
	private final Cache cache;
	private final Value<Logging> loggingValue = Value.synchronize(Logging.unified());
	private final ClientCore clientCore;

	NativeClientStart(SocketAddress address) {
		this.addressValue.set(address);
		communicationRegistration = CommunicationRegistration.open();
		cache = Cache.open();
		client = Client.create(communicationRegistration);
		client.setSession(Session.open(client));
		clientCore = ClientCore.nio();
		loggingValue.get().instantiated(this);
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
	public synchronized void launch() throws StartFailedException {
		if (running.get()) {
			loggingValue.get().warn("ClientStart is already started! Cannot start an already started NetworkInterface!");
		}
		SocketAddress socketAddress = addressValue.get();
		if (addressValue.isEmpty()) {
			throw new StartFailedException("Could not find requested SocketAddress to start this ClientStart!");
		}

		ClientDefaultCommunication.applyTo(this);
		clientCore.establishConnection(socketAddress, client);
		running.set(true);
		loggingValue.get().info("ClientStart started at " + socketAddress);
	}

	@Override
	public void startBlockerThread() {
		clientCore.startBlockerThread(this::running);
	}

	@Override
	public void blockOnCurrentThread() {
		loggingValue.get().trace("Requesting block on current Thread");
		clientCore.blockOnCurrentThread(this::running);
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
		clientCore.releaseBlocker();
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
