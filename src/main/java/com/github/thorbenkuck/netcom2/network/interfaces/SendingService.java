package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.interfaces.SoftStoppable;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

/**
 * The SendingService class is the entry-point for sending objects over the network.
 *
 * This Runnable is meant to be run in a separate Thread and to be (sort of) forgotten about. It is used within a {@link Connection}.
 *
 * @version 1.0
 * @since 1.0
 */
public interface SendingService extends Runnable, SoftStoppable {

	/**
	 * Adds a Callback, that will be executed, if the object is send
	 *
	 * @param callback the Callback, that should be held internally.
	 */
	void addSendDoneCallback(final Callback<Object> callback);

	/**
	 * Allows to override the internally saved instance of the BlockingQueue, that contains the Objects that should be send.
	 * <p>
	 * This means, if set to a custom instance, you can provide custom Objects to be send
	 *
	 * @param linkedBlockingQueue the new BlockingQueue
	 */
	void overrideSendingQueue(final BlockingQueue<Object> linkedBlockingQueue);

	/**
	 * Sets up this SendingService.
	 *
	 * This means, internal dependencies will be resolved and this SendingService is ready to run.
	 *
	 * @param outputStream the OutputStream, this SendingService should write to.
	 * @param toSendFrom   the BlockingQueue, that the Objects to send should be taken from
	 */
	void setup(final OutputStream outputStream, final BlockingQueue<Object> toSendFrom);

	/**
	 * Returns an Synchronization mechanism, that allows to wait until the SendingService is setup.
	 * <p>
	 * This mechanism stops to block if the SendingService is setup und running.
	 *
	 * @return the Synchronization mechanism.
	 */
	Awaiting started();

	/**
	 * Allows you to set a supplier, that defines the ConnectionID.
	 *
	 * @param supplier the Supplier, that creates the ConnectionID
	 */
	void setConnectionIDSupplier(Supplier<String> supplier);

	/**
	 * This method returns whether or not this SendingService is setup or not.
	 *
	 * If it is setup, it can successfully run. To set it up, call {@link #setup(OutputStream, BlockingQueue)}.
	 *
	 * @return true, if it was setup, else false
	 * @see #setup(OutputStream, BlockingQueue)
	 */
	boolean isSetup();
}
