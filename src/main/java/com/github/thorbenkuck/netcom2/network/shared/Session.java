package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.netcom2.interfaces.Mutex;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.heartbeat.HeartBeat;

import java.io.Serializable;
import java.util.Properties;

/**
 * This interface describes an Session of any Network-Object.
 * <p>
 * On the {@link ClientStart} side, this object is maintained, as
 * long, as the ClientStart stays connected to the Server.
 * <p>
 * On the {@link com.github.thorbenkuck.netcom2.network.server.ServerStart} side, this object is maintained within an {@link Client}
 * and shared across all Connection between each of the Connections of the Client.
 * <p>
 * If you wanted to, you could create an custom Session-Class and provide it to an Client using {@link Client#setSession(Session)}.
 * Note that especially the behaviour of the methods {@link #triggerPrimation()}, {@link #primed()} and {@link #newPrimation()}
 * should NOT be changed to maintain the Behaviour of the internal Mechanisms. Otherwise you might break the core Mechanisms between
 * Client-Server-Communication or Connection establishment.
 * <p>
 * Otherwise you can feel free to create an custom Session.
 */
public interface Session extends Mutex, Serializable {

	/**
	 * Returns a new instance of the internal implementation of this interface.
	 * <p>
	 * Based upon the given client, a {@link com.github.thorbenkuck.netcom2.interfaces.SendBridge}-implementation will
	 * be created to allow the Session to send objects.
	 * <p>
	 * The use of this Method is encouraged over instantiating the internal Session manually! The internal Implementation might
	 * change due to development reasons and might be swapped easy without breaking code using this method
	 *
	 * @param client the Client, which is connected to the Session
	 * @return an instance of the internal Session implementation
	 */
	static Session createNew(final Client client) {
		return new SessionImpl(new ClientSendBridge(client));
	}

	/**
	 * Describes whether or not this Session is identified
	 * <p>
	 * Value is controlled via {@link #setIdentified(boolean)}
	 * The Default value for this is: <b>false</b>
	 *
	 * @return if the session is identified
	 */
	boolean isIdentified();

	/**
	 * Sets this Sessions identification value.
	 * <p>
	 * It regulates the output of {@link #isIdentified()}
	 *
	 * @param identified the new boolean value
	 */
	void setIdentified(final boolean identified);

	/**
	 * Returns the unique identifier of this Session.
	 * <p>
	 * Value is controlled via {@link #setIdentifier(String)}
	 * The Default value for this is: <b>UUID.randomUUID() created inside of the SessionImpl constructor</b>
	 *
	 * @return the Identifier of this Session
	 */
	String getIdentifier();

	/**
	 * Sets this Sessions identifier value.
	 * <p>
	 * It regulates the output of {@link #getIdentifier()}
	 *
	 * @param identifier the new Identifier for this particular Session
	 */
	void setIdentifier(final String identifier);

	/**
	 * Returns the internal {@link Properties} instance.
	 * <p>
	 * This instance might be shared across multiple Sessions, but it is discouraged to send this instance to an ClientStart.
	 * Value is controlled via {@link #setProperties(Properties)}
	 * The Default value for this is: <b>new Properties()</b>
	 *
	 * @return the internal set of Properties
	 */
	Properties getProperties();

	/**
	 * Sets this Sessions internal {@link Properties} instance.
	 * <p>
	 * It regulates the output of {@link #getProperties()}
	 *
	 * @param properties the new {@link Properties} instance that this Session will use
	 */
	void setProperties(final Properties properties);

	/**
	 * Sends an Object over the Network.
	 * <p>
	 * This Method utilizes an {@link com.github.thorbenkuck.netcom2.interfaces.SendBridge} to send the given Object to the
	 * Server.
	 * This Method does no Sanity-Checks, neither does it work asynchronously! Its return depends completely on the implementation
	 * of the corresponding {@link Client} and the corresponding {@link com.github.thorbenkuck.netcom2.network.shared.clients.Connection}.
	 * <p>
	 * The mechanisms of Serialization, Encryption, s.o. therefor also depend on the corresponding {@link Client} and the
	 * corresponding {@link com.github.thorbenkuck.netcom2.network.shared.clients.Connection}.
	 * <p>
	 * Since this Method is not offering an Parameter to choose the Connection, the chosen Connection will be the Clients
	 * {@link com.github.thorbenkuck.netcom2.network.shared.clients.DefaultConnection}, which is initialized at the first
	 * connect of the corresponding ClientStart.
	 * <p>
	 * This Method waits for the Primation of the corresponding Client and therefor of this Session.
	 *
	 * @param o the Object that should be send over the Network.
	 */
	void send(final Object o);

	/**
	 * Prepares and returns an {@link Pipeline} instance for an given Class.
	 * <p>
	 * Those Events can be handled as you like. Since the returned Value is an Pipeline, you can edit as you like.
	 * Note that after an Pipeline has been created, you <b>cannot</b> delete it! You can clear the existing instance, but
	 * you cannot free up the memory space of that Pipeline
	 * <p>
	 * An event, encapsulated inside such an Pipeline can than be triggered using {@link #triggerEvent(Class, Object)}.
	 *
	 * @param clazz The Class, of which the Event is about
	 * @param <T>   Generic type about the type, which this Pipeline is going to handle
	 * @return An {@link Pipeline}-implementation, to add or modify the Event
	 */
	<T> Pipeline<T> eventOf(final Class<T> clazz);

	/**
	 * Triggers an before set Event, using {@link #eventOf(Class)}.
	 * <p>
	 * It searches for an corresponding Pipeline and if present runs it.
	 * If it cannot find an Pipeline a.e. no Event has been registered, an {@link IllegalArgumentException} is thrown.
	 * <p>
	 * The class is required, because of Javas type-erasure. Because <code>t.getClass()</code>, does not return an Class
	 * of the Type of the Object, but of ? extends Object.
	 *
	 * @param clazz the Identifier Class for this event
	 * @param t     an Object instance of the Class
	 * @param <T>   the Type of the Pipeline
	 * @throws IllegalArgumentException if no Pipeline is set
	 */
	<T> void triggerEvent(final Class<T> clazz, T t);

	/**
	 * Adds and holds an {@link HeartBeat}, which handles any Session.
	 * <p>
	 * Note that this method does not create an HeartBeat, it accepts any Instance of an HeartBeat and starts it asynchronous.
	 * Other than maintaining and running said HearBeat, this method and the Session in total does nothing else with an HeartBeat!
	 *
	 * @param heartBeat the HeartBeat that should be added to this Session
	 */
	void addHeartBeat(final HeartBeat<Session> heartBeat);

	/**
	 * Removes an previously set {@link HeartBeat} instance, using the {@link #addHeartBeat(HeartBeat)} method.
	 *
	 * @param heartBeat the HeartBeat that should be removed from this Session
	 */
	void removeHeartBeat(final HeartBeat<Session> heartBeat);

	/**
	 * This method is only used by the {@link Client}.
	 * The Primation is an internal Mechanism, to check the State, whether or not the current Session is ready to be used.
	 * As any new Client is created, it gets an new Instance of a Session, which by default is not primed. Than, after the
	 * Handshake between the ClientStart and the ServerStart, the Client is going to be Primed by calling
	 * {@link Client#triggerPrimation()}. This results in this Method being called.
	 * Internally, this method is further used whenever an new Connection is successfully established.
	 * <p>
	 * The use of this method is not forbidden, but discouraged since this might screw up the internal mechanisms
	 */
	void triggerPrimation();

	/**
	 * Returns the internal state, whether or not this Session is primed. This state is represented via the {@link Awaiting}
	 * interface and you can await it, by calling {@link Awaiting#synchronize()}.
	 * <p>
	 * This instance of Awaiting will continue, once {@link #triggerPrimation()} is called.
	 *
	 * @return The internal state, whether or not this Session is primed.
	 */
	Awaiting primed();

	/**
	 * Resets the internal primed state.
	 * This resets the Internal {@link Awaiting}.
	 * <p>
	 * The use of this Method is certainly not forbidden, but discouraged. Calling this method might screw up the internal mechanisms.
	 */
	void newPrimation();

	/**
	 * This is a functional style of updating the Session.
	 * <p>
	 * If you use this on the Client-Side, note that calling {@link SessionUpdater#sendOverNetwork()} will NOT update the
	 * Session on the Server-Side
	 *
	 * @return a new {@link SessionUpdater} instance for you to use.
	 */
	SessionUpdater update();
}
