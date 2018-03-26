package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.pipe.PipelineCondition;
import com.github.thorbenkuck.netcom2.annotations.Experimental;
import com.github.thorbenkuck.netcom2.interfaces.Loggable;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public interface Connection extends Loggable {

	/**
	 * Closes the Connection.
	 * <p>
	 * This will close the underlying connection-type. In the case of the {@link AbstractConnection}, this is the Socket.
	 * <p>
	 * It may throw an IOException, if anything goes wrong while closing the Connection.
	 */
	void close() throws IOException;

	/**
	 * Sets up internal dependencies.
	 * <p>
	 * Depending on the implementation, this requires an already established connection to the source.
	 * <p>
	 * In the Case of the {@link AbstractConnection}, this means it creates the {@link com.github.thorbenkuck.netcom2.network.interfaces.SendingService}
	 * and {@link com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService}, that listen on the Connection and
	 * handle Objects.
	 */
	void setup();

	/**
	 * Removes an set Consumer, that is called once the Connection disconnects.
	 *
	 * @param consumer the Consumer, that should be removed.
	 */
	void removeOnDisconnectedConsumer(final Consumer<Connection> consumer);

	/**
	 * Writes an Object to this Connection.
	 * <p>
	 * This means the Object is Send "to the other Side of the Connection".
	 * In fact, an Connection is only the entry/exit point. So writing an Connection means, it will be received by the
	 * corresponding exit point.
	 * <p>
	 * This method is used by the {@link Client}.
	 *
	 * @param object the Object that should be send over the Connection
	 */
	void write(final Object object);

	/**
	 * Adds an listener, that will be called once an Object, added with {@link #write(Object)}, was successfully send.
	 *
	 * @param callback the Callback, that should be called, once the Object is called.
	 */
	void addObjectSendListener(final Callback<Object> callback);

	/**
	 * Adds an listener, that will be called once an Object, was received.
	 * <p>
	 * These Callbacks will be send after the CommunicationRegistration is finished to handel the Object.
	 *
	 * @param callback the Callback, that should be called, once the Object is called.
	 */
	void addObjectReceivedListener(final Callback<Object> callback);

	/**
	 * This Method sets the {@link ExecutorService}, that the {@link com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService}
	 * and the {@link com.github.thorbenkuck.netcom2.network.interfaces.SendingService} are executed in.
	 * <p>
	 * NOTE: Currently this does NOT work!
	 *
	 * @param executorService the ExecutorService, that should be used.
	 */
	@Experimental
	void setThreadPool(final ExecutorService executorService);

	/**
	 * This Method returns an internally {@link Awaiting} instance, that unlocks once this Connection successfully listens.
	 *
	 * @return the internal Synchronization Mechanism
	 */
	Awaiting startListening();

	/**
	 * Adds an Consumer, that should handled once this Connection disconnects.
	 * <p>
	 * All of those Consumers are executed within an Pipeline.
	 *
	 * @param consumer the Consumer, that should consume this Connection, once this Connection is disconnected
	 * @return an {@link PipelineCondition}, that you may use to state whether or not the Consumer should be executed
	 */
	PipelineCondition<Connection> addOnDisconnectedConsumer(final Consumer<Connection> consumer);

	/**
	 * Returns the {@link InputStream} for this Connection
	 *
	 * @return the InputStream for this Connection
	 * @throws IOException if something goes wrong
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Returns the {@link OutputStream} for this Connection
	 *
	 * @return the OutputStream for this Connection
	 * @throws IOException if something goes wrong
	 */
	OutputStream getOutputStream() throws IOException;

	/**
	 * Returns the Object sending queue.
	 * <p>
	 * Objects are added to this Blocking queue. If you want to just dump any Object into this Connection, you may use this.
	 * The {@link com.github.thorbenkuck.netcom2.network.interfaces.SendingService} will take this Object (sooner or later)
	 * and Send it.
	 * <p>
	 * Note, that no Callback Object will be added.
	 *
	 * @return an internally maintained SendingQueue
	 */
	BlockingQueue<Object> getSendInterface();

	/**
	 * Returns the Session, this Connection is identified with.
	 *
	 * @return the Session for this Connection
	 */
	Session getSession();

	/**
	 * Sets the Session, this Connection is identified with.
	 *
	 * @param session the Session, this Connection should be identified with
	 */
	void setSession(final Session session);

	/**
	 * Returns an formatted address for printing
	 *
	 * @return an pretty formatted String
	 */
	String getFormattedAddress();

	/**
	 * Returns the Port, this Connection is bound to
	 *
	 * @return the port
	 */
	int getPort();

	/**
	 * Returns the {@link InetAddress} for the Destination of the Connection
	 *
	 * @return the InetAddress for this Connection
	 */
	InetAddress getInetAddress();

	/**
	 * Returns whether or not, this Connection is actively sending and receiving Objects
	 *
	 * @return whether or not, this Connection is active.
	 */
	boolean isActive();

	/**
	 * Returns the identifier for this Connection, which is a Class.
	 *
	 * @return the Key for this Connection
	 */
	Class<?> getKey();

	/**
	 * Sets the Key for this Connection, which is a Class
	 *
	 * @param connectionKey the new class, that should identify this Connection
	 */
	void setKey(final Class<?> connectionKey);
}
