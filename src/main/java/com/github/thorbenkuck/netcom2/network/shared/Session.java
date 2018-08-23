package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.netcom2.interfaces.Mutex;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

import java.util.Properties;

public interface Session extends Mutex {

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
	static Session open(final Client client) {
		return open(SendBridge.openTo(client));
	}

	static Session open(SendBridge sendBridge) {
		return new NativeSession(sendBridge);
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
	 * of the corresponding {@link Client} and the corresponding {@link com.github.thorbenkuck.netcom2.network.shared.connections.Connection}.
	 * <p>
	 * The mechanisms of Serialization, Encryption, s.o. therefor also depend on the corresponding {@link Client} and the
	 * corresponding {@link com.github.thorbenkuck.netcom2.network.shared.connections.Connection}.
	 * <p>
	 * Since this Method is not offering an Parameter to choose the Connection, the chosen Connection will be the Clients
	 * {@link com.github.thorbenkuck.netcom2.network.shared.connections.DefaultConnection}, which is initialized at the first
	 * connect of the corresponding ClientStart.
	 * <p>
	 * This Method waits for the Primation of the corresponding Client and therefor of this Session.
	 *
	 * @param o the Object that should be send over the Network.
	 */
	void send(final Object o);

	/**
	 * This method is only used by the {@link Client}.
	 * The Primation is an internal Mechanism, to check the State, whether or not the current Session is ready to be used.
	 * As any new Client is created, it gets an new Instance of a Session, which by default is not primed. Than, after the
	 * Handshake between the ClientStart and the ServerStart, the Client is going to be Primed by calling
	 * {@link Client}. This results in this Method being called.
	 * Internally, this method is further used whenever an new Connection is successfully established.
	 * <p>
	 * The use of this method is not forbidden, but discouraged since this might screw up the internal mechanisms
	 *
	 * @deprecated misleading name
	 */
	@Deprecated
	default void triggerPrimation() {
		triggerPrimed();
	}

	void triggerPrimed();

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
	 *
	 * @deprecated misleading name
	 */
	@Deprecated
	default void newPrimation() {
		resetPrimed();
	}

	void resetPrimed();
}
