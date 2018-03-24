package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.interfaces.SoftStoppable;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

public interface ReceivingService extends Runnable, SoftStoppable {

	/**
	 * This method cleans up all save CallBacks, leached to this ReceivingService.
	 * <p>
	 * Calling this Method, will clean out all Callbacks, that return true on {@link Callback#isRemovable()}
	 */
	void cleanUpCallBacks();

	/**
	 * Adds an Callback, that will be handled internally
	 *
	 * @param callback the Callback, that should be used
	 */
	void addReceivingCallback(final Callback<Object> callback);

	/**
	 * Sets up internal Components.
	 * <p>
	 * It is therefor branded to the provided Connection and Session.
	 * This is needed for the {@link com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration}
	 *
	 * @param connection the Connection, this ReceivingService listens to
	 * @param session    the Session, this ReceivingService listens to
	 */
	void setup(final Connection connection, final Session session);

	/**
	 * Sets the Session.
	 *
	 * @param session the Session
	 */
	void setSession(final Session session);

	/**
	 * Sets what to do, if the ReceivingService is disconnected
	 *
	 * @param runnable the Runnable, that should be run if this disconnected
	 */
	void onDisconnect(final Runnable runnable);

	/**
	 * Returns an synchronization mechanism, that will stop blocking if this is listening
	 *
	 * @return the internal Synchronization mechanism
	 */
	Awaiting started();

	boolean isSetup();
}
