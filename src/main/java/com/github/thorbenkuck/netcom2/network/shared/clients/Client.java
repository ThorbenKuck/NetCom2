package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.network.client.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.client.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import javafx.beans.DefaultProperty;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * An Client is an Object-Representation of an physically Computer, connected via one or multiple {@link java.net.Socket}.
 *
 * The Client encapsulates multiple informations needed for the Client-Server-Communication, such as:
 * <li>
 *     <ul>Connections this Computer uses</ul>
 *     <ul>Serialization and FallbackSerialization</ul>
 *     <ul>A Session, shared across all active Connections</ul>
 *     <ul>A Disconnected Handler</ul>
 *     <ul>The CommunicationRegistration</ul>
 * </li>
 *
 * The client has multiple methods, that are required for the internal Mechanisms, so that the default behaviour is working.
 * Especially Connections, Serialization, Encryption, Primed and FalseIDs are core elements for the internal Mechanisms.
 *
 * It is highly discouraged to create custom Client-Objects.
 *
 * The Client is create, once a Socket connects and maintained within the {@link com.github.thorbenkuck.netcom2.network.server.ClientList}
 *
 * Most of the Time, you do not need to do anything with this class, except for setting Encryption or Synchronization.
 * Some of the Methods are highly risky to use, except if followed by a certain other call.
 */
public interface Client {

	/**
	 * Returns a new instance of the internal implementation of this interface.
	 * <p>
	 * Based upon the given {@link CommunicationRegistration}, the ClientImplementation will be created and returned.
	 * <p>
	 * The use of this Method is encouraged over instantiating the internal Client manually! The internal Implementation might
	 * change due to development reasons and might be swapped easy without breaking code using this method
	 *
	 * @param communicationRegistration the {@link CommunicationRegistration}, which is used by all Connections to handle received Objects
	 * @return an instance of the internal Client implementation
	 */
	static Client create(CommunicationRegistration communicationRegistration) {
		return new ClientImpl(communicationRegistration);
	}

	/**
	 * This Method is meant for providing an custom {@link ExecutorService} to be used within every subclass of  the Client.
	 *
	 * For example the {@link Connection} provides a method to override the existing ThreadPool.
	 *
	 * WARNING! This Method is extremely workload intensive! For this Method to work correctly, all current {@link Runnable}
	 * have to be stopped and than the current ExecutorService has to be shutdown. This procedure is repeated for each subclass, like the Connections.
	 * In this Time-Frame all {@link com.github.thorbenkuck.netcom2.network.interfaces.SendingService} and {@link com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService} will be stopped,
	 * which will lead to an shut down communication during the time the ExecutorService is swapped!
	 *
	 * @param executorService the new ExecutorService to be used within every sub class.
	 */
	void setThreadPool(ExecutorService executorService);

	/**
	 * This Method initially set's up the Client.
	 *
	 * It creates a new {@link Session} and potentially overrides the existing one. Calling this Method might be a bad idea.
	 * It certainly is possible to call this Method at runtime, be the Session has to be passed to every Connection.
	 *
	 * Further: You should call <code>client.getSession().triggerPrimation()</code> after the this method returns. The new Session is NOT primed.
	 *
	 * Also, if you call this method during an Connection establishment or anything similar, you might screw up the currently
	 * running Mechanism, which requires the Session.
	 *
	 * This method is to be understood as the initial setup of an Client. It is called upon its creation and not again later.
	 */
	void setup();

	/**
	 * This Method cuts all Connections.
	 *
	 * This Methods closes all Connections, created at any point in time, and afterwards clears the list which those where safed in.
	 * After all Connections are closed, the disconnected handler will be called in an descending order of {@link DisconnectedHandler#getPriority()}.
	 * Each of the {@link DisconnectedHandler} will only be called, if {@link DisconnectedHandler#active()} returns true.
	 * Lastly the ClientID will be set to an {@link ClientID#empty()} and the Session will be recreated
	 *
	 * Calling this method is not reversible! Once closed, it cannot be reopened easily.
	 *
	 * This will disconnect the ClientStart and ServerStart respectively from each other.
	 */
	void disconnect();

	/**
	 * @see Session#triggerPrimation()
	 */
	void triggerPrimation();

	/**
	 * @see Session#primed()
	 */
	Awaiting primed();

	/**
	 * @see Session#newPrimation()
	 */
	void newPrimation();

	/**
	 * Returns the internal Session for this Client.
	 * The Session is created upon calling {@link #setup()}.
	 * The Client knows of its Session, but the Session does not knows of its Client. The Session however uses an {@link com.github.thorbenkuck.netcom2.network.server.ClientSendBridge}
	 * which is individual for each Client. Therefor every Session is unique for every Client, even tho multiple Session may have the same attribute.
	 *
	 * @see Session
	 *
	 * This might return null, if {@link #clearSession()} is called! A client without a Session cannot function correctly!
	 * Therefor calling this method might destroy the internal behaviour
	 *
	 * @return the internally set {@link Session}
	 */
	Session getSession();

	/**
	 * Sets the internal Session, overriding any previously set instances.
	 *
	 * @param session the current internal set Session or null.
	 */
	void setSession(Session session);

	/**
	 * Deletes the internal Session.
	 *
	 * The Session will be set to null! This is important! If you call this method {@link #getSession()} WILL return null!
	 *
	 * You should ONLY call this method, if you experience difficulties or something similar whenever an Client disconnects.
	 * Otherwise you will get a LOT of {@link NullPointerException} from nearly everywhere inside NetCom2.
	 *
	 * Most remarkably you will produce a {@link NullPointerException} whenever you registered a {@link com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive} or {@link com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple}
	 * which uses the injected Session and is received from the Client responsible for this Session.
	 *
	 * This method is called by the {@link com.github.thorbenkuck.netcom2.network.client.DefaultClientDisconnectedHandler}
	 * to reset the internal Client from within the {@link com.github.thorbenkuck.netcom2.network.interfaces.ClientStart}
	 * so that you might reconnect and reuse this client. At the ServerSide this will lead to problems.
	 *
	 * To counterweight the call of clear Session you might call {@link #setup()}, to set a new Session instance.
	 */
	void clearSession();

	/**
	 * Adds an {@link DisconnectedHandler} to this Client, which is called if this Client disconnects from the ClientStart or ServerStart respectively.
	 *
	 * The DisconnectedHandler is therefore a CallBackObject.
	 *
	 * @param disconnectedHandler an instance of an DisconnectedHandler.
	 */
	void addDisconnectedHandler(DisconnectedHandler disconnectedHandler);

	/**
	 * This Method will try to send an Object over an Connection.
	 *
	 * Since this Method takes no identifier parameter for the Connection, it uses the {@link DefaultConnection} to Send the method.
	 * This Method returns nearly immediately. Further, an Object is considered "send" if the Connection accepts the Object.
	 *
	 * For your convenience you might use the returned {@link ReceiveOrSendSynchronization} and wait until the Object is send
	 * or another Object is received over the Connection
	 *
	 * @param object the Object to be send over the Connection, must meet the Connection requirements to be send successfully
	 * @return an ReceiveOrSendSynchronization for Synchronizing this asynchronous process.
	 */
	ReceiveOrSendSynchronization send(Object object);

	/**
	 * Behaves similar to the {@link #send(Object)} method with the exception, that it takes the connectionKey of an existing
	 * Connection to be send over this Connection instead.
	 *
	 * @see #send(Object)
	 *
	 * @param connectionKey the Key of the Connection, this object should be send over
	 * @param object the Object to be send over the Connection, must meet the Connection requirements to be send successfully
	 * @return an ReceiveOrSendSynchronization for Synchronizing this asynchronous process.
	 */
	ReceiveOrSendSynchronization send(Class connectionKey, Object object);

	/**
	 * Behaves similar to the {@link #send(Object)} method with the exception, that it takes the concrete Connection the given Object
	 * should be send over.
	 *
	 * @see #send(Object)
	 *
	 * @param connection the Connection the Object should be send over
	 * @param object the Object to be send over the Connection, must meet the Connection requirements to be send successfully
	 * @return an ReceiveOrSendSynchronization for Synchronizing this asynchronous process.
	 */
	ReceiveOrSendSynchronization send(Connection connection, Object object);

	/**
	 * Returns an respective Connection for a given ConnectionKey.
	 *
	 * Since this Connection might not (yet) exist, it is wrapped in an Optional.
	 *
	 * @param connectionKey the Key for the Connection
	 * @return the Optional.of(connection for connectionKey)
	 */
	Optional<Connection> getConnection(Class connectionKey);

	/**
	 * Creates an new Connection based upon an Class.
	 *
	 * A call on the ServerStart will result in an Request to create a new Connection on the ClientStart.
	 * A call on the ClientStart will result in an Request to ask to create a new Connection on the ServerStart which is
	 * functionally equal to this call on the ServerStart
	 *
	 * Once the Request ist send from the ServerStart to the ClientStart, a new physical connection is going to be established.
	 * This whole Process is completely Asynchronous and can be synchronized using the returned {@link Awaiting#synchronize()}.
	 *
	 * This results in an long Request-Response-Chain from ClientStart to ServerStart. On the end of this Chain, the returned Awaiting
	 * is triggered to continue and the Connection is ready to be used.
	 *
	 * The key is an Class, so that it is assured it can be send over the Network.
	 *
	 * @param connectionKey a Class as an key to identify the Connection.
	 * @return an {@link Awaiting} that continues if the Connection is established and primed.
	 */
	Awaiting createNewConnection(Class connectionKey);

	/**
	 * Returns a random Connection from this Client
	 *
	 * This is method is one of the potentially least used methods of this whole class
	 *
	 * @return a random Connection from this Client
	 */
	Connection getAnyConnection();

	/**
	 * Returns the formatted address for this Client.
	 *
	 * This is only used for printing and in the following form:
	 *
	 * <code>inetAddress() + ":" + port</code>
	 *
	 * @return the formatted Address of this Client.
	 */
	String getFormattedAddress();

	/**
	 * Returns the {@link ClientID} for this Client
	 *
	 * This Method will never Return null and is controlled by {@link #setID(ClientID)}
	 *
	 * @return the ClientID for this Client
	 */
	ClientID getID();

	/**
	 * Sets the {@link ClientID} for this client.
	 *
	 * This might not be null and will throw an {@link IllegalArgumentException} if null is provided.
	 * You can certainly call this method, but it is highly discouraged to do so. The idea of this method is, to manually
	 * override the ClientID of false Clients, created via a new Connection creation.
	 *
	 * @throws IllegalArgumentException if id == null
	 * @param id the new ID for this client
	 */
	void setID(ClientID id);

	/**
	 * Safes an connection for this Client.
	 *
	 * Neither the provided key, nor the Connection itself might be null.
	 * The Connection will than be able to be used by {@link #send(Class, Object)} or {@link #getConnection(Class)}.
	 * If you insert a custom Connection here, you will have to ensure, that this Connection is correctly established.
	 * This method does not ensure, that the Connection is correctly established.
	 *
	 * The Connection might be of any Type (Client-Server, File, or anything similar), but it must be active if set.
	 *
	 * Also you might rout any other Connection by setting an already existing Connection to a new Key.
	 * For Example:
	 * <code>
	 *     client.getConnection(FirstKey.class).ifPresent(connection -> client.setConnection(SecondKey.class, connection));
	 * </code>
	 *
	 * This will allow the connection to be used / retrieved by both:
	 *
	 * <code>
	 *     client.getConnection(FirstKey.class).get();
	 *     client.getConnection(SecondKey.class).get();
	 * </code>
	 *
	 * An both will result in the same Connection
	 *
	 * @param key the key, through which the Connection can be used
	 * @param connection the Connection, that should be used, when asked for the Connection
	 */
	void setConnection(Class key, Connection connection);

	/**
	 * Returns the {@link CommunicationRegistration} used by this Client.
	 *
	 * Since this Client has one or more {@link Connection} established, which have on {@link com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService},
	 * which require the CommunicationRegistration this Client encapsulates the CommunicationRegistration
	 *
	 * @return the used CommunicationRegistration by this Client
	 */
	CommunicationRegistration getCommunicationRegistration();

	/**
	 * Sets the Main-{@link SerializationAdapter} to be used by the {@link Connection}, encapsulated by this Client.
	 *
	 * @param mainSerializationAdapter the new MainSerializationAdapter
	 */
	void setMainSerializationAdapter(SerializationAdapter<Object, String> mainSerializationAdapter);

	/**
	 * Sets the Main-{@link DeSerializationAdapter} to be used by the {@link Connection}, encapsulated by this Client.
	 *
	 * @param mainDeSerializationAdapter the new Main-DeSerializationAdapter
	 */
	void setMainDeSerializationAdapter(DeSerializationAdapter<String, Object> mainDeSerializationAdapter);

	/**
	 * This method sets the internal List of FallBackSerializationAdapter, without overriding the existing ones.
	 *
	 * @param fallBackSerializationAdapter a List containing multiple {@link SerializationAdapter} instances
	 */
	void addFallBackSerializationAdapter(List<SerializationAdapter<Object, String>> fallBackSerializationAdapter);

	/**
	 * @see #addFallBackSerializationAdapter(List)
	 * @deprecated use {@link #addFallBackSerializationAdapter(List)}
	 * @param fallBackSerializationAdapter a List containing multiple {@link SerializationAdapter} instances
	 */
	@Deprecated
	void setFallBackSerializationAdapter(List<SerializationAdapter<Object, String>> fallBackSerializationAdapter);

	/**
	 * This method sets the internal List of FallBackDeSerializationAdapter, without overriding the existing ones.
	 *
	 * @param fallBackDeSerializationAdapter a List containing multiple {@link DeSerializationAdapter} instances
	 */
	void addFallBackDeSerializationAdapter(List<DeSerializationAdapter<String, Object>> fallBackDeSerializationAdapter);

	/**
	 * @see #addFallBackDeSerializationAdapter(List)
	 * @deprecated use {@link #addFallBackDeSerializationAdapter(List)}
	 * @param fallBackDeSerializationAdapter a List containing multiple {@link DeSerializationAdapter} instances
	 */
	@Deprecated
	void setFallBackDeSerializationAdapter(List<DeSerializationAdapter<String, Object>> fallBackDeSerializationAdapter);

	void addFallBackSerialization(SerializationAdapter<Object, String> serializationAdapter);

	void addFallBackDeSerialization(DeSerializationAdapter<String, Object> deSerializationAdapter);

	void setEncryptionAdapter(EncryptionAdapter encryptionAdapter);

	void setDecryptionAdapter(DecryptionAdapter decryptionAdapter);

	SerializationAdapter<Object, String> getMainSerializationAdapter();

	DeSerializationAdapter<String, Object> getMainDeSerializationAdapter();

	Set<SerializationAdapter<Object, String>> getFallBackSerialization();

	Set<DeSerializationAdapter<String, Object>> getFallBackDeSerialization();

	DecryptionAdapter getDecryptionAdapter();

	EncryptionAdapter getEncryptionAdapter();

	Awaiting prepareConnection(Class clazz);

	boolean isConnectionPrepared(Class clazz);

	void notifyAboutPreparedConnection(Class clazz);

	void addFalseID(ClientID clientID);

	List<ClientID> getFalseIDs();

	void removeFalseID(ClientID clientID);

	void removeFalseIDs(List<ClientID> clientIDS);
}
