package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.*;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.DefaultConnection;
import com.github.thorbenkuck.netcom2.network.shared.connections.RawData;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * An Client is an Object-Representation of a physical Computer, connected via one or multiple {@link java.net.Socket}.
 * <p>
 * The Client encapsulates information needed for the Client-Server-Communication, such as:
 * <ul>
 * <li>Connections this Computer uses</li>
 * <li>A Session, shared across all active Connections</li>
 * <li>A Disconnected Handler</li>
 * <li>The CommunicationRegistration</li>
 * </ul>
 * <p>
 * The client has multiple methods, that are required for the internal Mechanisms, so that the default behaviour is working.
 * Especially Connections, Serialization, Encryption, Primed and FalseIDs are core elements for the internal Mechanisms.
 * <p>
 * It is highly discouraged to create custom Client-Objects.
 * <p>
 * The Client is created, once a Socket connects and maintained within the {@link com.github.thorbenkuck.netcom2.network.server.ClientList}
 * <p>
 * Most of the Time, you do not need to do anything with this class, except for setting Encryption or Synchronization.
 * Some of the Methods are highly risky to use, except if followed by a certain other call.
 * <p>
 * <b>Version 1.1:</b>
 * <p>
 * With version 1.1, the Serialization and Encryption mechanisms, are decoupled into the {@link ObjectHandler}.
 * <p>
 * The Connection management also has been overhauled. A Client now is considered primed, as soon as the DefaultConnection
 * is established.
 * <p>
 * All currently deprecated methods will be removed in the next major update.
 *
 * @version 1.1
 * @see Session
 * @see CommunicationRegistration
 * @see ObjectHandler
 * @since 1.0
 */
public interface Client {

	static Client create(CommunicationRegistration communicationRegistration) {
		return new NativeClient(communicationRegistration);
	}

	/**
	 * Removes a Connection from the internally maintained ConnectionMap.
	 * <p>
	 * This is only needed, if you manually add a Connection to the Client.
	 *
	 * @param connection the Connection, that should be removed
	 */
	void removeConnection(Connection connection);

	/**
	 * Adds a connection.
	 * <p>
	 * Internally it uses the {@link #setConnection(Class, Connection)} method, based on the {@link Connection#getIdentifier()}
	 * value.
	 *
	 * @param connection the Connection, that should be added.
	 */
	void addConnection(Connection connection);

	/**
	 * Safes an connection for this Client.
	 * <p>
	 * Neither the provided key, nor the Connection itself might be null.
	 * The Connection will than be able to be used by {@link #send(Class, Object)} or {@link #getConnection(Class)}.
	 * If you insert a custom Connection here, you will have to ensure, that this Connection is correctly established.
	 * This method does not ensure, that the Connection is correctly established.
	 * <p>
	 * The Connection might be of any Type (Client-Server, File, or anything similar), but it must be active if set.
	 * <p>
	 * Also you might rout any other Connection by setting an already existing Connection to a new Key.
	 * For Example:
	 * <code>
	 * client.getConnection(FirstKey.class).ifPresent(connection - client.setConnection(SecondKey.class, connection));
	 * </code>
	 * <p>
	 * This will allow the connection to be used / retrieved by both:
	 * <p>
	 * <code>
	 * client.getConnection(FirstKey.class).get();
	 * client.getConnection(SecondKey.class).get();
	 * </code>
	 * <p>
	 * An both will result in the same Connection
	 *
	 * @param identifier the key, through which the Connection can be used
	 * @param connection the Connection, that should be used, when asked for the Connection
	 */
	void setConnection(Class<?> identifier, Connection connection);

	/**
	 * Searches for the set {@link Connection}s for the originalKey.
	 *
	 * @param originalKey a key, the chosen {@link Connection} is set to
	 * @param newKey      the new key, which this {@link Connection} should be accessible through
	 * @see #routeConnection(Connection, Class)
	 */
	void routeConnection(final Class originalKey, final Class newKey);

	/**
	 * This Method routs an given {@link Connection} to an new Key.
	 * <p>
	 * The Original {@link Connection} will not be unbound from its current bound. This means, that after calling this method,
	 * the given {@link Connection} is accessible via both, its original Key and the newKey.
	 * <p>
	 * A {@link Connection} might be routed to any number of keys. So one {@link Connection} can be accessible by any number of calls.
	 * <p>
	 * Other than {@link #setConnection(Class, Connection)} a "null-route" is possible, to allow an sort of "fallback-route".
	 * <p>
	 * If you use:
	 * <code>client.routConnection(OriginalKey.class, null);</code>
	 * a warning will be logged via the {@link com.github.thorbenkuck.netcom2.logging.Logging} and the Connection is
	 * used, whenever you state:
	 * <code>client.send(new MessageObject(), null);</code>
	 * <p>
	 * This might be useful, if you calculate the Keys at runtime. However, it is discouraged to trigger a null-route
	 * by stating null at compile time.
	 * <p>
	 * Implementing aspects: Implementing this Method should not lead to an duplication of this {@link Connection}. The route should
	 * be implemented the same way, the original {@link Connection} setting was.
	 * <p>
	 * Further should this rout be accessible by "not complex calculations" (negative example: setting it inside the {@link Connection}
	 * and than iterating over all {@link Connection}, comparing each set Key inside this {@link Connection} to find
	 * each corresponding {@link Connection}).
	 *
	 * @param originalConnection the {@link Connection} that should be rerouted
	 * @param newKey             the new key, under which the given {@link Connection} is accessible
	 */
	void routeConnection(final Connection originalConnection, final Class newKey);

	/**
	 * Returns an respective Connection for a given ConnectionKey.
	 * <p>
	 * Since this Connection might not (yet) exist, it is wrapped in an Optional.
	 *
	 * @param connectionKey the Key for the Connection
	 * @return the Optional.of(connection for connectionKey)
	 */
	Optional<Connection> getConnection(final Class connectionKey);

	/**
	 * This Method cuts all Connections.
	 * <p>
	 * In detail: it closes all Connections, created at any point in time, and afterwards clears the list which those where kept in.
	 * After all Connections are closed, the disconnected handler will be called in an descending order of how they where added
	 * Each of the {@link ClientDisconnectedHandler} will be called.
	 * <p>
	 * Lastly the ClientID will be set to an {@link ClientID#empty()} and the Session will be recreated
	 * <p>
	 * Calling this method is not reversible! Once closed, it cannot be reopened easily. The only way would be, to manually
	 * recreate all Connections, required for this Client to function. So calling this method manually is discouraged
	 * <p>
	 * That being said, this Method is called by the ClientStart, if the Server disconnects after an Connection has been
	 * established. Internally this is done, to prevent calculations to recreate the Client once this ClientStart reconnects.
	 * <p>
	 * This will disconnect the ClientStart and ServerStart respectively.
	 * <p>
	 * Note: The behaviour of this Method changed in V.1.1. It will now trigger a pipeline, containing the disconnected Handler.
	 */
	void disconnect();

	/**
	 * Returns the internal Session for this Client.
	 * The Session is created upon calling {@link #setup()}.
	 * The Client knows of its Session, but the Session does not knows of its Client. The Session however uses an {@link com.github.thorbenkuck.netcom2.interfaces.SendBridge}
	 * which is individual for each Client. Therefor every Session is unique for every Client, even tho multiple Session may have the same attribute.
	 *
	 * @return the internally set {@link Session}
	 * @see Session
	 */
	Session getSession();

	/**
	 * Sets the internal Session, overriding any previously set instances.
	 *
	 * @param session the current internal set Session or null.
	 */
	void setSession(final Session session);

	/**
	 * Returns the {@link CommunicationRegistration} used by this Client.
	 *
	 * @return the used CommunicationRegistration by this Client
	 */
	CommunicationRegistration getCommunicationRegistration();

	/**
	 * Returns an Awaiting instance over the primed state
	 * <p>
	 * This Awaiting will block, until the defaultConnection is established
	 *
	 * @return a Awaiting instance
	 */
	Awaiting primed();

	/**
	 * Describes whether or not this Client is primed.
	 *
	 * @return true if this Client is primed, else false
	 */
	boolean isPrimed();

	/**
	 * Triggers the primed state of this Client.
	 * <p>
	 * You may call this at any time, but you should not. This will be called automatically, once the DefaultConnection
	 * is established.
	 * <p>
	 * If this client is already primed, this call is ignored.
	 */
	void triggerPrimed();

	/**
	 * Returns the {@link ClientID} for this Client
	 * <p>
	 * This Method will never Return null and is controlled by {@link #setID(ClientID)}
	 *
	 * @return the ClientID for this Client
	 */
	ClientID getID();

	/**
	 * Sets the {@link ClientID} for this client.
	 * <p>
	 * This might not be null and will throw an {@link IllegalArgumentException} if null is provided.
	 * You can certainly call this method, but it is highly discouraged to do so. The idea of this method is, to manually
	 * override the ClientID of false Clients, created via a new Connection creation.
	 *
	 * @param id the new ID for this client
	 * @throws IllegalArgumentException if id == null
	 */
	void setID(final ClientID id);

	void receive(RawData rawData, Connection connection);

	/**
	 * Adds an {@link ClientDisconnectedHandler} to this Client, which is called if this Client disconnects from the ClientStart or ServerStart respectively.
	 * <p>
	 * The DisconnectedHandler is therefore a CallBackObject.
	 * <p>
	 * Note: Previously, this Method expected a DisconnectedHandler. We now use a {@link ClientDisconnectedHandler}, because
	 * it inherits from Consumer and may easily be added to a Pipeline, without the need of a wrapper
	 *
	 * @param disconnectedHandler an instance of an DisconnectedHandler.
	 */
	void addDisconnectedHandler(ClientDisconnectedHandler disconnectedHandler);

	void removeDisconnectedHandler(ClientDisconnectedHandler disconnectedHandler);

	void addPrimedCallback(Consumer<Client> clientConsumer);

	ObjectHandler objectHandler();

	/**
	 * Whilst the other send Methods wait for the Connection to de block {@link Connection#connected()}, this method ignores this.
	 * <p>
	 * Some times it is needed, but most of the time, this is not what you want. Instead, try to use {@link #send(Object, Class)},
	 * {@link #send(Object, Connection)} or {@link #send(Object)}.
	 * <p>
	 * The {@link Connection#connected()} {@link Awaiting} blocks until the internal default CommunicationProtocol is finished.
	 * Only then it is ensured, that the Server or the Client knows exactly what this Connection is associated with.
	 *
	 * @param object     the Object to send over the Connection
	 * @param connection the Connection to send the Object to
	 */
	void sendIgnoreConstraints(Object object, Connection connection);

	/**
	 * Behaves similar to the {@link #send(Object)} method with the exception, that it takes the concrete Connection the given Object
	 * should be send over.
	 * <p>
	 * This Method is the exit-point of every other send call.
	 *
	 * @param connection the Connection the Object should be send over
	 * @param object     the Object to be send over the Connection, must meet the Connection requirements to be send successfully
	 * @see #send(Object)
	 */
	void send(Object object, Connection connection);

	/**
	 * Behaves similar to the {@link #send(Object)} method with the exception, that it takes the connectionKey of an existing
	 * Connection to be send over this Connection instead.
	 * <p>
	 * Note: Previously, this method returned a ReceiveOrSendSynchronization. This is no longer the case.
	 *
	 * @param object        the Object to be send over the Connection, must meet the Connection requirements to be send successfully
	 * @param connectionKey the Key of the Connection, this object should be send over
	 * @see #send(Object)
	 */
	void send(Object object, Class<?> connectionKey);

	/**
	 * This Method will try to send an Object over an Connection.
	 * <p>
	 * Since this Method takes no identifier parameter for the Connection, it uses the {@link com.github.thorbenkuck.netcom2.network.shared.connections.DefaultConnection}
	 * to Send the method. This Method returns nearly immediately. Further, an Object is considered "send" if the
	 * Connection accepts the Object.
	 * <p>
	 * In the new Design, this method ultimately extracts a new Task into the {@link NetComThreadPool} using
	 * {@link NetComThreadPool#submitTask(Runnable)}. If you experience to slow processing, call {@link NetComThreadPool#startWorkerProcess()}
	 * to start a new Worker.
	 * <p>
	 * Note: Previously, this method returned a ReceiveOrSendSynchronization. This is no longer the case.
	 *
	 * @param object the Object to be send over the Connection, must meet the Connection requirements to be send successfully
	 * @see NetComThreadPool#submitTask(Runnable)
	 * @see NetComThreadPool#startWorkerProcess()
	 */
	void send(final Object object);

	/**
	 * Returns the formatted address for this Client.
	 * <p>
	 * This is only used for printing and in the following form:
	 * <p>
	 * <code>inetAddress() + ":" + port</code>
	 *
	 * @return the formatted Address of this Client.
	 */
	String getFormattedAddress();

	void overridePrepareConnection(Class clazz, Synchronize synchronize);

	Synchronize accessPrepareConnection(Class clazz);

	/**
	 * Prepares a {@link Connection} to be established soon.
	 * <p>
	 * This method creates an internal {@link Awaiting}, to be used, to synchronize until the given {@link Connection} is established.
	 * You can manually use this Method, to ensure any {@link Connection} you manually establish is set and ready to go.
	 * <p>
	 * If your Manual {@link Connection} is ready to go, the method {@link #notifyAboutPreparedConnection(Class)}. This will release
	 * any Threads, waiting for the establishment of said {@link Connection}.
	 * <p>
	 * Please note, that this method should be called with care. If you prepare an Connection, that is already established
	 * you might screw up the internal Mechanisms"
	 * <p>
	 * Also note that you do not have to call this method, if you use the {@link #createNewConnection(Class)} method. Upon calling
	 * the {@link #createNewConnection(Class)} the internal Mechanisms will sooner or later call this method to create an
	 * Awaiting to synchronize Threads, waiting for this Connection.
	 * <p>
	 * Do not cast this return value to an {@link Synchronize}! The instance
	 * of the Awaiting is depending on the Implementation of the Client.
	 *
	 * @param clazz the key for the new {@link Connection}, that is going to be established
	 * @return the {@link Awaiting} to synchronize and wait for the Connection to be established.
	 */
	Awaiting prepareConnection(final Class clazz);

	/**
	 * Creates an new Connection based upon an Class.
	 * <p>
	 * A call on the ServerStart will result in an Request to create a new Connection on the ClientStart.
	 * A call on the ClientStart will result in an Request to ask to create a new Connection on the ServerStart which is
	 * functionally equal to this call on the ServerStart
	 * <p>
	 * Once the Request ist send from the ServerStart to the ClientStart, a new physical connection is going to be established.
	 * This whole Process is completely Asynchronous and can be synchronized using the returned {@link Awaiting#synchronize()}.
	 * <p>
	 * This results in an long Request-Response-Chain from ClientStart to ServerStart. On the end of this Chain, the returned Awaiting
	 * is triggered to continue and the Connection is ready to be used.
	 * <p>
	 * The key is an Class, so that it is assured it can be send over the Network.
	 *
	 * @param connectionKey a Class as an key to identify the Connection.
	 * @return an {@link Awaiting} that continues if the Connection is established and primed.
	 */
	default Awaiting createNewConnection(final Class connectionKey) {
		send(new NewConnectionRequest(connectionKey));
		return prepareConnection(connectionKey);
	}

	void connectionPrepared(Class<?> identifier);

	void invalidate();

	/*
	 * Following are old Methods, which are now deprecated.
	 *
	 * Each method has been replaced or reworked and will be implemented with default.
	 *
	 * Still, even though most of this might still work, it is highly recommended, to use the new Design, instead of the
	 * old Design.
	 */

	/**
	 * This Method is meant for providing an custom {@link ExecutorService} to be used within every subclass of  the Client.
	 * <p>
	 * For example the {@link Connection} provides a method to override the existing ThreadPool.
	 * <p>
	 * WARNING! This Method is extremely workload intensive! For this Method to work correctly, all current {@link Runnable}
	 * have to be stopped and than the current ExecutorService has to be shutdown. This procedure is repeated for each subclass, like the Connections.
	 * In this Time-Frame all {} and {} will be stopped,
	 * which will lead to an shut down communication during the time the ExecutorService is swapped!
	 *
	 * @param executorService the new ExecutorService to be used within every sub class.
	 * @deprecated The Client will no longer manually create Threads. It uses {@link NetComThreadPool}
	 */
	@Deprecated
	default void setThreadPool(final ExecutorService executorService) {
	}

	/**
	 * This Method initially sets up the Client.
	 * <p>
	 * It creates a new {@link Session} and potentially overrides the existing one. Calling this Method might be a bad idea.
	 * It certainly is possible to call this Method at runtime, but the Session has to be passed to every Connection.
	 * <p>
	 * Further: You should call <code>client.getSession().triggerPrimation()</code> after the this method returns.
	 * The new Session, now associated after this method finishes, is NOT primed.
	 * <p>
	 * Also, if you call this method during an Connection establishment or anything similar, you might screw up the currently
	 * running Mechanism, which requires the Session.
	 * <p>
	 * This method is to be understood as the initial setup of an Client. It is called upon its creation and not again later.
	 * <p>
	 * Implementation Aspects: Call this Method within the constructor, or make clear, that it has to be called as soon as
	 * possible. Further you might disable any other call, after the first.
	 *
	 * @deprecated Old Design. Use {@link #triggerPrimed()} upon first Connection creation
	 */
	@Deprecated
	default void setup() {
	}

	/**
	 * @see Session#triggerPrimation()
	 * @deprecated use {@link #triggerPrimed()}
	 */
	@Deprecated
	default void triggerPrimation() {
		triggerPrimed();
	}

	/**
	 * @see Session#newPrimation()
	 */
	@Deprecated
	default void newPrimation() {

	}

	/**
	 * Deletes the internal Session.
	 * <p>
	 * The Session will be set to null! This is important! If you call this method {@link #getSession()} WILL return null!
	 * <p>
	 * You should ONLY call this method, if you experience difficulties or something similar whenever an Client disconnects.
	 * Otherwise you will get a LOT of {@link NullPointerException} from nearly everywhere inside NetCom2.
	 * <p>
	 * This method is called by the DefaultClientDisconnectedHandler
	 * to reset the internal Client from within the {@link com.github.thorbenkuck.netcom2.network.client.ClientStart}
	 * so that you might reconnect and reuse this client. At the ServerSide this will lead to problems.
	 * <p>
	 * To counterweight the call of clear Session you might call {@link #setup()}, to set a new Session instance.
	 */
	@Deprecated
	default void clearSession() {
	}

	/**
	 * Behaves similar to the {@link #send(Object)} method with the exception, that it takes the connectionKey of an existing
	 * Connection to be send over this Connection instead.
	 *
	 * @param connectionKey the Key of the Connection, this object should be send over
	 * @param object        the Object to be send over the Connection, must meet the Connection requirements to be send successfully
	 * @see #send(Object)
	 * @deprecated this Method has been replace by {@link #send(Object, Class)} because of the wording
	 */
	@Deprecated
	default void send(Class connectionKey, Object object) {
		send(object, connectionKey);
	}

	/**
	 * Behaves similar to the {@link #send(Object)} method with the exception, that it takes the concrete Connection the given Object
	 * should be send over.
	 *
	 * @param connection the Connection the Object should be send over
	 * @param object     the Object to be send over the Connection, must meet the Connection requirements to be send successfully
	 * @see #send(Object)
	 * @deprecated this Method has been replace by {@link #send(Object, Connection)} because of the wording
	 */
	@Deprecated
	default void send(final Connection connection, final Object object) {
		send(object, connection);
	}

	/**
	 * Returns a random Connection from this Client
	 * <p>
	 * This is method is one of the potentially least used methods of this whole class
	 *
	 * @return a random Connection from this Client
	 * @deprecated This Method is way to calculation intensive for its task.
	 */
	@Deprecated
	default Connection getAnyConnection() {
		return getConnection(DefaultConnection.class).orElseThrow(IllegalStateException::new);
	}

	/**
	 * This method sets the internal List of FallBackSerializationAdapter, without overriding the existing ones.
	 *
	 * @param fallBackSerializationAdapter a List containing multiple {@link SerializationAdapter} instances
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default void addFallBackSerializationAdapter(final List<SerializationAdapter> fallBackSerializationAdapter) {
		objectHandler().addFallbackSerialization(fallBackSerializationAdapter);
	}

	/**
	 * This method sets the internal List of FallBackDeSerializationAdapter, without overriding the existing ones.
	 *
	 * @param fallBackDeSerializationAdapter a List containing multiple {@link DeSerializationAdapter} instances
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default void addFallBackDeSerializationAdapter(final List<DeSerializationAdapter> fallBackDeSerializationAdapter) {
		objectHandler().addFallbackDeserialization(fallBackDeSerializationAdapter);
	}

	/**
	 * Adds an FallbackSerialization in form of an SerializationAdapter.
	 * <p>
	 * All Adapter added this way, are tested if the {@link #setMainSerializationAdapter(SerializationAdapter)} fails to serialize
	 * the given Object.
	 *
	 * @param serializationAdapter an SerializationAdapter to be used, if the main Adapter fails
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default void addFallBackSerialization(final SerializationAdapter serializationAdapter) {
		objectHandler().addFallbackSerialization(serializationAdapter);
	}

	/**
	 * Adds an FallbackDeSerialization in form of an DeSerializationAdapter.
	 * <p>
	 * All Adapter added this way, are tested if the {@link #setMainDeSerializationAdapter(DeSerializationAdapter)} fails to deserialize
	 * the given Object.
	 *
	 * @param deSerializationAdapter an DeSerializationAdapter to be used, if the main Adapter fails
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default void addFallBackDeSerialization(final DeSerializationAdapter deSerializationAdapter) {
		objectHandler().addFallbackDeserialization(deSerializationAdapter);
	}

	/**
	 * @return the set MainSerializationAdapter
	 * @see #setMainSerializationAdapter(SerializationAdapter)
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default SerializationAdapter getMainSerializationAdapter() {
		throw new UnsupportedOperationException("Deprecated");
	}

	/**
	 * Sets the Main-{@link SerializationAdapter} to be used by the {@link Connection}, encapsulated by this Client.
	 * <p>
	 * This will take the object, that should be send over the chosen {@link Connection} and turns it into an String.
	 * This String is than send over the Network and then DeSerialized, using the {@link #setMainDeSerializationAdapter(DeSerializationAdapter)}.
	 * <p>
	 * The String should be reversibly serialized, so that it can be turned back into an Object with its current state.
	 *
	 * @param mainSerializationAdapter the new MainSerializationAdapter
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default void setMainSerializationAdapter(final SerializationAdapter mainSerializationAdapter) {
		objectHandler().setMainSerialization(mainSerializationAdapter);
	}

	/**
	 * @return the set MainDeSerializationAdapter
	 * @see #setMainDeSerializationAdapter(DeSerializationAdapter)
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default DeSerializationAdapter getMainDeSerializationAdapter() {
		throw new UnsupportedOperationException("Deprecated!");
	}

	/**
	 * Sets the Main-{@link DeSerializationAdapter} to be used by the {@link Connection}, encapsulated by this Client.
	 * <p>
	 * This will take the string, that was received over an {@link Connection} and turns it into an Object, which is than passed anto the {@link CommunicationRegistration}.
	 * This String was formally serialized using the {@link #setMainSerializationAdapter(SerializationAdapter)}.
	 *
	 * @param mainDeSerializationAdapter the new Main-DeSerializationAdapter
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default void setMainDeSerializationAdapter(final DeSerializationAdapter mainDeSerializationAdapter) {
		objectHandler().setMainDeserialization(mainDeSerializationAdapter);
	}

	/**
	 * @return the set Set of all Fallback SerializationAdapter
	 * @see #addFallBackSerialization(SerializationAdapter)
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default Set<SerializationAdapter> getFallBackSerialization() {
		return Collections.unmodifiableSet(new HashSet<>());
	}

	/**
	 * @return the set Set of all Fallback DeSerializationAdapter
	 * @see #addFallBackDeSerialization(DeSerializationAdapter)
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default Set<DeSerializationAdapter> getFallBackDeSerialization() {
		return Collections.unmodifiableSet(new HashSet<>());
	}

	/**
	 * @return the set set DecryptionAdapter
	 * @see #setDecryptionAdapter(DecryptionAdapter)
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default DecryptionAdapter getDecryptionAdapter() {
		throw new UnsupportedOperationException("Deprecated");
	}

	/**
	 * Sets an {@link DecryptionAdapter} to be used to decrypt strings after they were deSerialized using an {@link DeSerializationAdapter}.
	 *
	 * @param decryptionAdapter the {@link DecryptionAdapter} that should be used in all {@link Connection} of this Client
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default void setDecryptionAdapter(final DecryptionAdapter decryptionAdapter) {
		objectHandler().addDecryptionAdapter(decryptionAdapter);
	}

	/**
	 * @return the set set EncryptionAdapter
	 * @see #setEncryptionAdapter(EncryptionAdapter)
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default EncryptionAdapter getEncryptionAdapter() {
		throw new UnsupportedOperationException("Deprecated");
	}

	/**
	 * Sets an {@link EncryptionAdapter} to be used to encrypt strings after they were serialized using an {@link SerializationAdapter}.
	 * <p>
	 * This encryption should be reversibly, so that it can be turned back into its original state using the {@link DecryptionAdapter}.
	 *
	 * @param encryptionAdapter the {@link EncryptionAdapter} that should be used in all {@link Connection} of this Client
	 * @deprecated has been relocated to the {@link ObjectHandler}
	 */
	@Deprecated
	default void setEncryptionAdapter(final EncryptionAdapter encryptionAdapter) {
		objectHandler().addEncryptionAdapter(encryptionAdapter);
	}

	/**
	 * Returns, whether or not an Connection is prepared.
	 * <p>
	 * This Method will return true, if an Connection is prepared, but not yet established.
	 * You can prepare an connection by calling {@link #prepareConnection(Class)}.
	 * You can release the prepared part, by calling {@link #notifyAboutPreparedConnection(Class)}. Once you called this Method,
	 * the internal Awaiting will continue and not be safed any longer.
	 * <p>
	 * Tho you can save the Awaiting, returned by {@link #prepareConnection(Class)}, it is discouraged. It will be cleared
	 * automatically
	 *
	 * @param clazz the identifier of the Connection
	 * @return whether or not the Connection is prepared but not released
	 */
	@Deprecated
	default boolean isConnectionPrepared(final Class clazz) {
		return getConnection(clazz).isPresent();
	}

	/**
	 * Releases the internal {@link Awaiting} instance, waiting for any Connection prepared, identified by the parameter.
	 * <p>
	 * You can access the {@link Awaiting} by calling {@link #prepareConnection(Class)} with the same class. It will
	 * return the corresponding Awaiting.
	 *
	 * @param clazz the Class
	 * @see #prepareConnection(Class)
	 * <p>
	 * This Method may release the internal {@link Awaiting}, but the implementation of the Awaiting is defined by the implementation
	 * of this Client.
	 */
	@Deprecated
	default void notifyAboutPreparedConnection(final Class clazz) {
	}

	/**
	 * Adds an faulty ID, that this Client is falsely associated with.
	 * <p>
	 * It is recommended to not use this Method. This Method is required, by the internal Mechanism to create and establish
	 * new Connections.
	 * <p>
	 * Since the establishment of a new Connection means the establishment of a new Socket, a new Client is falsely create
	 * for each new Connection. Those Clients need to be deleted at from the {@link com.github.thorbenkuck.netcom2.network.server.ClientList},
	 * these {@link ClientID} added this way, are used to find those false Clients.
	 * <p>
	 * Therefor the correct Client will encapsulate all false {@link ClientID}s.
	 *
	 * @param clientID the {@link ClientID} of an Client, that was created falsely
	 */
	@Deprecated
	default void addFalseID(final ClientID clientID) {
	}

	/**
	 * Returns all false {@link ClientID}.
	 *
	 * @return a List of all ClientIDs, identifying false Clients
	 * @see #addFalseID(ClientID)
	 */
	@Deprecated
	default List<ClientID> getFalseIDs() {
		return Collections.unmodifiableList(new ArrayList<>());
	}

	/**
	 * Removes a false ID from this Client.
	 *
	 * @param clientID the ClientID, that should be removed
	 * @see #addFalseID(ClientID)
	 */
	@Deprecated
	default void removeFalseID(final ClientID clientID) {
	}

	/**
	 * Removes all given false IDs from this Client.
	 *
	 * @param clientIDS a list, containing ClientIDs, that should be removed
	 * @see #removeFalseID(ClientID)
	 */
	@Deprecated
	default void removeFalseIDs(final List<ClientID> clientIDS) {
	}
}
