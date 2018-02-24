package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.Experimental;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.interfaces.Factory;
import com.github.thorbenkuck.netcom2.interfaces.MultipleConnections;
import com.github.thorbenkuck.netcom2.interfaces.RemoteObjectRegistration;
import com.github.thorbenkuck.netcom2.interfaces.SoftStoppable;
import com.github.thorbenkuck.netcom2.network.handler.ClientConnectedHandler;
import com.github.thorbenkuck.netcom2.network.interfaces.Launch;
import com.github.thorbenkuck.netcom2.network.interfaces.Loggable;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

public interface ServerStart extends Launch, SoftStoppable, Loggable, MultipleConnections {

	/**
	 * Creates a new ServerStart at the provided port.
	 *
	 * This is the only way currently of creating a ServerStart.
	 *
	 * You may however feel free to create your own.
	 *
	 * @param port the port, the Server shut letch onto
	 * @return a new Instance of the ServerStart
	 */
	static ServerStart at(final int port) {
		return new ServerStartImpl(new ServerConnector(port));
	}

	/**
	 * Calling this Method will block, until the Server is shutdown or until an exception occurs.
	 *
	 * This method is accepting all Clients. While doing so, this call blocks.
	 *
	 * If you want to use this method in an non-blocking way, use the {@link com.github.thorbenkuck.netcom2.utility.NetCom2Utils#runLater(Runnable)}
	 * or {@link com.github.thorbenkuck.netcom2.utility.NetCom2Utils#runOnNetComThread(Runnable)} method.
	 *
	 * NOTE: If you extract this Method into another NetComThread, most of the Time, your Program will just exit.
	 * The NetComThreads are daemon threads, which means, that they will not stop the Program from exiting if they are still running.
	 * A best Practice would be, to use your own Thread to extract this.
	 *
	 * By default, this is the main Way of using the ServerStart. If you require some Work between each client connection,
	 * use the {@link #acceptNextClient()} method, which only accepts the next client.
	 *
	 * An {@link ClientConnectionFailedException} will be thrown, if the Creation of the Client fails or any {@link ClientConnectedHandler}
	 * throws an Exception. The last one will result in an stop of the whole Server, to allow you to fix the issue.
	 *
	 * @throws ClientConnectionFailedException if anything goes wrong while a Client connects
	 * @see #acceptNextClient()
	 */
	void acceptAllNextClients() throws ClientConnectionFailedException;

	/**
	 * Accepts the next Client tha connects.
	 *
	 * This Method-Call will block, until the next Client is connected.
	 * You will use this, if you need to doe some Work between each Client connection.
	 *
	 * An {@link ClientConnectionFailedException} will be thrown, if the Creation of the Client fails or any {@link ClientConnectedHandler}
	 * throws an Exception.
	 *
	 * @throws ClientConnectionFailedException if anything goes wrong while a Client connects
	 */
	void acceptNextClient() throws ClientConnectionFailedException;

	/**
	 * Sets the port of the ServerStart.
	 *
	 * Allows you, to set the Port, even after the ServerStart has been created.
	 * If you need to change the port after the ServerStart is created, use this.
	 *
	 * @param port the port the ServerStart should letch onto
	 */
	void setPort(final int port);

	/**
	 * Adds an {@link ClientConnectedHandler}, that should handle a newly created Client.
	 *
	 * Those ClientConnectedHandlers will be asked 2 times. First to create the Client-Object and second to handle this Object.
	 *
	 * @param clientConnectedHandler the Client ConnectedHandler that should be usd
	 */
	void addClientConnectedHandler(final ClientConnectedHandler clientConnectedHandler);

	/**
	 * Removes a ClientConnectedHandler from the ServerStart.
	 *
	 * @param clientConnectedHandler the ClientConnectedHandler
	 */
	void removeClientConnectedHandler(final ClientConnectedHandler clientConnectedHandler);

	/**
	 * Returns an {@link Distributor} instance, to Distribute without knowing the specific Session of the Client.
	 *
	 * This Distributor is used, if you want to send something to multiple Targets.
	 *
	 * For most other needs, you should use the Session, that is injected into the OnReceive handlers or use the
	 * {@link ClientConnectedHandler}
	 *
	 * @return an internally maintained instance of the Distributor
	 * @see Distributor
	 */
	Distributor distribute();

	/**
	 * Returns a {@link Cache} instance, to set and save specific elements.
	 *
	 * This cache is connected to the Distributor, so that updating and setting objects results in an Distribution to all
	 * Clients that want to be notified about new or updated instances.
	 *
	 * @return an internally maintained instance of the Cache
	 * @see Cache
	 */
	Cache cache();

	/**
	 *
	 */
	void disconnect();

	void setServerSocketFactory(final Factory<Integer, ServerSocket> factory);

	ClientList clientList();

	CommunicationRegistration getCommunicationRegistration();

	void setExecutorService(final ExecutorService executorService);

	@Experimental
	RemoteObjectRegistration remoteObjects();
}
