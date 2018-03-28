package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.pipe.PipelineCondition;
import com.github.thorbenkuck.netcom2.annotations.Experimental;
import com.github.thorbenkuck.netcom2.interfaces.Loggable;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * A Connection is an object-representation of an physical connection between the {@link com.github.thorbenkuck.netcom2.network.client.ClientStart}
 * and the {@link com.github.thorbenkuck.netcom2.network.server.ServerStart}.
 * <p>
 * Such a Connection is build in an similar way as the {@link java.nio}-package. The idea is, that you have a {@link com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService},
 * that handles incoming objects asynchronously and an {@link com.github.thorbenkuck.netcom2.network.interfaces.SendingService}
 * that handles outgoing objects asynchronously.
 * <p>
 * Both are Runnable classes and will use {@link com.github.thorbenkuck.netcom2.utility.NetCom2Utils} for extracting
 * procedures into other Threads.
 *
 * @version 1.0
 * @see ConnectionFactory
 * @since 1.0
 */
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
	 * In the case of the {@link AbstractConnection}, this means it creates the {@link com.github.thorbenkuck.netcom2.network.interfaces.SendingService}
	 * and {@link com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService}, that listen on the Connection and
	 * handles send/received Objects.
	 */
	void setup();

	/**
	 * Removes an set {@link Consumer}, that is called once the Connection disconnects.
	 *
	 * @param consumer the {@link Consumer}, that should be removed.
	 * @see Client#addDisconnectedHandler(DisconnectedHandler)
	 * @see #addOnDisconnectedConsumer(Consumer)
	 */
	void removeOnDisconnectedConsumer(final Consumer<Connection> consumer);

	/**
	 * Writes an Object to this Connection.
	 * <p>
	 * This means the Object is send "to the other side of the Connection".
	 * In fact, an Connection is only the entry/exit point. So writing an Connection means, it will be received by another
	 * instance of the same type, which represents the corresponding exit point.
	 * <p>
	 * This method is used by the {@link Client}.
	 *
	 * @param object the Object that should be send over the Connection.
	 *               @see Client#send(Object)
	 *               @see Client#send(Class, Object)
	 *               @see Client#send(Connection, Object)
	 */
	void write(final Object object);

	/**
	 * Adds a listener, that will be called once an Object, added with {@link #write(Object)}, was successfully send.
	 *
	 * @param callback the {@link Callback}, that should be called, once the Object is called.
	 */
	void addObjectSendListener(final Callback<Object> callback);

	/**
	 * Adds an listener, that will be called once a Object, was received.
	 * <p>
	 * These {@link Callback} will be triggered after the {@link com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration}
	 * is finished to handel the received Object.
	 *
	 * @param callback the {@link Callback}, that should be called, once a Object is received.
	 */
	void addObjectReceivedListener(final Callback<Object> callback);

	/**
	 * This method sets the {@link ExecutorService}, that the {@link com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService}
	 * and the {@link com.github.thorbenkuck.netcom2.network.interfaces.SendingService} are executed in.
	 * <p>
	 * NOTE: Currently this does NOT work!
	 *
	 * @param executorService the ExecutorService, that should be used.
	 */
	@Experimental
	void setThreadPool(final ExecutorService executorService);

	/**
	 * This method returns an internally {@link Awaiting} instance, that unlocks once this Connection is successfully listens.
	 *
	 * @return the internal synchronization mechanism.
	 */
	Awaiting startListening();

	/**
	 * Adds an  {@link Consumer}, that should be trigger, once this Connection disconnects.
	 * <p>
	 * All of those {@link Consumer consumers} are executed within an {@link com.github.thorbenkuck.keller.pipe.Pipeline}.
	 *
	 * @param consumer the Consumer, that should consume this Connection, once this Connection is disconnected.
	 * @return an {@link PipelineCondition}, that you may use to state whether or not the {@link Consumer} should be executed.
	 */
	PipelineCondition<Connection> addOnDisconnectedConsumer(final Consumer<Connection> consumer);

	/**
	 * Returns the {@link InputStream} for this Connection.
	 *
	 * @return the {@link InputStream} for this Connection.
	 * @throws IOException if something goes wrong.
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Returns the {@link OutputStream} for this Connection.
	 *
	 * @return the {@link OutputStream} for this Connection.
	 * @throws IOException if something goes wrong.
	 */
	OutputStream getOutputStream() throws IOException;

	/**
	 * Returns the Object sending queue.
	 * <p>
	 * Objects are added to this {@link BlockingQueue}. If you want to just dump any Object into this Connection, you may use this.
	 * The {@link com.github.thorbenkuck.netcom2.network.interfaces.SendingService} will take this Object (sooner or later)
	 * and send it.
	 * <p>
	 * Note, that no {@link Callback} will be added.
	 *
	 * @return an internally maintained send interface in the type of an {@link BlockingQueue}.
	 */
	BlockingQueue<Object> getSendInterface();

	/**
	 * Returns the {@link Session}, this Connection is identified with.
	 *
	 * @return the {@link Session} for this Connection.
	 */
	Session getSession();

	/**
	 * Sets the {@link Session}, this Connection is identified with.
	 *
	 * @param session the {@link Session}, this Connection should be identified with.
	 */
	void setSession(final Session session);

	/**
	 * Returns an formatted address for printing.
	 *
	 * @return an pretty formatted String.
	 */
	String getFormattedAddress();

	/**
	 * Returns the port, this Connection is bound to.
	 *
	 * @return the port.
	 */
	int getPort();

	/**
	 * Returns the {@link InetAddress} for the Destination of the Connection.
	 *
	 * @return the {@link InetAddress} for this Connection.
	 */
	InetAddress getInetAddress();

	/**
	 * Returns whether or not, this Connection is actively sending and receiving Objects.
	 *
	 * @return whether or not, this Connection is active.
	 */
	boolean isActive();

	/**
	 * Returns the identifier for this Connection, which is a {@link Class}.
	 *
	 * @return the Key for this Connection.
	 */
	Class<?> getKey();

	/**
	 * Sets the Key for this Connection, which is a {@link Class}.
	 *
	 * @param connectionKey the new class, that should identify this Connection.
	 */
	void setKey(final Class<?> connectionKey);
}
