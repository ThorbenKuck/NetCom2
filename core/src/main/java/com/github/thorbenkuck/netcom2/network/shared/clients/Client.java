package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.RawData;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.util.Optional;
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
	 * The Connection will than be able to be used by {@link #send(Object, Class)} or {@link #getConnection(Class)}.
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
	 * The Client knows of its Session, but the Session does not knows of its Client. The Session however uses a {@link com.github.thorbenkuck.netcom2.network.shared.SendBridge}
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
	 * If your Manual {@link Connection} is ready to go, the method {@link #connectionPrepared(Class)}. This will release
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
}
