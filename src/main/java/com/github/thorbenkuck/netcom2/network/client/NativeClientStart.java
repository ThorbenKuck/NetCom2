package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.*;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.client.ClientDisconnectedHandler;

import java.net.SocketAddress;

public class NativeClientStart implements ClientStart {

	private final SocketAddress address;

	public NativeClientStart(SocketAddress address) {
		this.address = address;
	}

	/**
	 * Used to send Objects to the ServerStart.
	 *
	 * @return an instance of the {@link Sender} interface
	 * @see Sender
	 */
	@Override
	public Sender send() {
		return null;
	}

	/**
	 * Adds a {@link SerializationAdapter} as a fallback serialization instance to this ClientStart.
	 *
	 * @param serializationAdapter the adapter, that should be used.
	 * @see SerializationAdapter
	 */
	@Override
	public void addFallBackSerialization(SerializationAdapter serializationAdapter) {

	}

	/**
	 * Adds a {@link DeSerializationAdapter} as a fallback deserialization instance to this ClientStart.
	 *
	 * @param deSerializationAdapter the adapter, that should be used.
	 * @see DeSerializationAdapter
	 */
	@Override
	public void addFallBackDeSerialization(DeSerializationAdapter deSerializationAdapter) {

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

	}

	/**
	 * Sets an Adapter for decryption of received Strings.
	 *
	 * @param decryptionAdapter the DecryptionAdapter
	 * @see DecryptionAdapter
	 */
	@Override
	public void setDecryptionAdapter(DecryptionAdapter decryptionAdapter) {

	}

	/**
	 * Sets an Adapter for encryption of Strings that should be send.
	 *
	 * @param encryptionAdapter the EncryptionAdapter
	 * @see EncryptionAdapter
	 */
	@Override
	public void setEncryptionAdapter(EncryptionAdapter encryptionAdapter) {

	}

	/**
	 * This Method is a shortcut for: {@link Cache#reset()}
	 *
	 * @see Cache#reset()
	 */
	@Override
	public void clearCache() {

	}

	@Override
	public Cache cache() {
		return null;
	}

	@Override
	public CommunicationRegistration getCommunicationRegistration() {
		return null;
	}

	@Override
	public void launch() throws StartFailedException {

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

	}

	/**
	 * Defines, whether or not the inheriting class is Running.
	 *
	 * @return true, if {@link #softStop()} was not called yet, else false
	 */
	@Override
	public boolean running() {
		return false;
	}

	/**
	 * Returns the internally maintained {@link RemoteObjectFactory}.
	 * <p>
	 * This method will never return null.
	 *
	 * @return the internally maintained instance of a RemoteObjectFactory.
	 */
	@Override
	public RemoteObjectFactory getRemoteObjectFactory() {
		return null;
	}

	/**
	 * This Method changes the {@link InvocationHandlerProducer} set internally.
	 * <p>
	 * The {@link InvocationHandlerProducer} creates a new Instance if {@link #getRemoteObject(Class)} is called.
	 * By changing this {@link InvocationHandlerProducer}, you can provide a custom {@link RemoteObjectHandler}
	 * to be used internally
	 *
	 * @param invocationHandlerProducer the {@link InvocationHandlerProducer} that should create the {@link RemoteObjectHandler}
	 * @see InvocationHandlerProducer
	 */
	@Override
	public void updateRemoteInvocationProducer(InvocationHandlerProducer invocationHandlerProducer) {

	}
}
