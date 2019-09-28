package com.github.thorbenkuck.netcom2.shared;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.utils.NetCom2Utils;

import java.util.Properties;
import java.util.concurrent.Semaphore;

public class NativeSession implements Session {

	private final SendBridge sendBridge;
	private final Value<Boolean> identifiedValue = Value.synchronize(false);
	private final Value<String> identifierValue = Value.synchronize("");
	private final Value<Properties> propertiesValue = Value.synchronize(new Properties());
	private final Synchronize synchronize = Synchronize.createDefault();
	private final Semaphore mutex = new Semaphore(1);
	private final Logging logging = Logging.unified();

	public NativeSession(SendBridge sendBridge) {
		this.sendBridge = sendBridge;
		logging.instantiated(this);
	}

	/**
	 * Describes whether or not this Session is identified
	 * <p>
	 * Value is controlled via {@link #setIdentified(boolean)}
	 * The Default value for this is: <b>false</b>
	 *
	 * @return if the session is identified
	 */
	@Override
	public boolean isIdentified() {
		return identifiedValue.get();
	}

	/**
	 * Sets this Sessions identification value.
	 * <p>
	 * It regulates the output of {@link #isIdentified()}
	 *
	 * @param identified the new boolean value
	 */
	@Override
	public void setIdentified(boolean identified) {
		identifiedValue.set(identified);
	}

	/**
	 * Returns the unique identifier of this Session.
	 * <p>
	 * Value is controlled via {@link #setIdentifier(String)}
	 * The Default value for this is: <b>UUID.randomUUID() created inside of the SessionImpl constructor</b>
	 *
	 * @return the Identifier of this Session
	 */
	@Override
	public String getIdentifier() {
		return identifierValue.get();
	}

	/**
	 * Sets this Sessions identifier value.
	 * <p>
	 * It regulates the output of {@link #getIdentifier()}
	 *
	 * @param identifier the new Identifier for this particular Session
	 */
	@Override
	public void setIdentifier(String identifier) {
		identifierValue.set(identifier);
	}

	/**
	 * Returns the internal {@link Properties} instance.
	 * <p>
	 * This instance might be shared across multiple Sessions, but it is discouraged to send this instance to an ClientStart.
	 * Value is controlled via {@link #setProperties(Properties)}
	 * The Default value for this is: <b>new Properties()</b>
	 *
	 * @return the internal set of Properties
	 */
	@Override
	public Properties getProperties() {
		return propertiesValue.get();
	}

	/**
	 * Sets this Sessions internal {@link Properties} instance.
	 * <p>
	 * It regulates the output of {@link #getProperties()}
	 *
	 * @param properties the new {@link Properties} instance that this Session will use
	 */
	@Override
	public void setProperties(Properties properties) {
		propertiesValue.set(properties);
	}

	/**
	 * Sends an Object over the Network.
	 * <p>
	 * This Method utilizes an {@link SendBridge} to send the given Object to the
	 * Server.
	 * <p>
	 * This Method waits for the Primation of the corresponding Client and therefor of this Session.
	 *
	 * @param o the Object that should be send over the Network.
	 */
	@Override
	public void send(Object o) {
		NetCom2Utils.parameterNotNull(o);
		sendBridge.send(o);
	}

	@Override
	public void triggerPrimed() {
		synchronize.goOn();
	}

	/**
	 * Returns the internal state, whether or not this Session is primed. This state is represented via the {@link Awaiting}
	 * interface and you can await it, by calling {@link Awaiting#synchronize()}.
	 * <p>
	 * This instance of Awaiting will continue, once {@link #triggerPrimed()} is called.
	 *
	 * @return The internal state, whether or not this Session is primed.
	 */
	@Override
	public Awaiting primed() {
		return synchronize;
	}

	/**
	 * Resets the internal primed state.
	 * This resets the Internal {@link Awaiting}.
	 * <p>
	 * The use of this Method is certainly not forbidden, but discouraged. Calling this method might screw up the internal mechanisms.
	 */
	@Override
	public void resetPrimed() {
		synchronize.reset();
	}

	/**
	 * Acquires access over the object. If any other Object has access over the given Object, this Methods waits until
	 * the current owner calls release.
	 *
	 * @throws InterruptedException if the waiting takes to long.
	 */
	@Override
	public void acquire() throws InterruptedException {
		mutex.acquire();
	}

	/**
	 * Releases the access over the Object and invoking any waiting Threads.
	 */
	@Override
	public void release() {
		mutex.release();
	}

	@Override
	public String toString() {
		return "NativeSession{" +
				"identifiedValue=" + identifiedValue.get() +
				", identifierValue=" + identifierValue.get() +
				'}';
	}
}
