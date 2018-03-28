package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.Experimental;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.interfaces.Factory;
import com.github.thorbenkuck.netcom2.interfaces.Loggable;
import com.github.thorbenkuck.netcom2.interfaces.MultipleConnections;
import com.github.thorbenkuck.netcom2.interfaces.SoftStoppable;
import com.github.thorbenkuck.netcom2.network.interfaces.ClientConnectedHandler;
import com.github.thorbenkuck.netcom2.network.interfaces.Launch;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

/**
 * This interface describes the Server-side of NetCom2.
 * <p>
 * As most other components, this interface has an implementation, that is hidden for different Reasons.
 *
 * @version 1.0
 * @since 1.0
 */
public interface ServerStart extends Launch, SoftStoppable, Loggable, MultipleConnections {

	/**
	 * Creates a new ServerStart at the provided port.
	 * <p>
	 * This is the only way currently of creating a ServerStart.
	 * <p>
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
	 * <p>
	 * This method is accepting all Clients. While doing so, this call blocks.
	 * <p>
	 * If you want to use this method in an non-blocking way, use the {@link com.github.thorbenkuck.netcom2.utility.NetCom2Utils#runLater(Runnable)}
	 * or {@link com.github.thorbenkuck.netcom2.utility.NetCom2Utils#runOnNetComThread(Runnable)} method.
	 * <p>
	 * NOTE: If you extract this Method into another NetComThread, most of the Time, your Program will just exit.
	 * The NetComThreads are daemon threads, which means, that they will not stop the Program from exiting if they are still running.
	 * A best Practice would be, to use your own Thread to extract this.
	 * <p>
	 * By default, this is the main Way of using the ServerStart. If you require some Work between each client connection,
	 * use the {@link #acceptNextClient()} method, which only accepts the next client.
	 * <p>
	 * An {@link ClientConnectionFailedException} will be thrown, if the Creation of the Client fails or any {@link ClientConnectedHandler}
	 * throws an Exception. The last one will result in an stop of the whole Server, to allow you to fix the issue.
	 *
	 * @throws ClientConnectionFailedException if anything goes wrong while a Client connects
	 * @see #acceptNextClient()
	 */
	void acceptAllNextClients() throws ClientConnectionFailedException;

	/**
	 * Accepts the next Client tha connects.
	 * <p>
	 * This Method-Call will block, until the next Client is connected.
	 * You will use this, if you need to doe some Work between each Client connection.
	 * <p>
	 * An {@link ClientConnectionFailedException} will be thrown, if the Creation of the Client fails or any {@link ClientConnectedHandler}
	 * throws an Exception.
	 *
	 * @throws ClientConnectionFailedException if anything goes wrong while a Client connects
	 */
	void acceptNextClient() throws ClientConnectionFailedException;

	/**
	 * Returns the current port of the ServerStart.
	 *
	 * @return the port this ServerStart uses
	 */
	int getPort();

	/**
	 * Sets the port of the ServerStart.
	 * <p>
	 * Allows you, to set the Port, even after the ServerStart has been created.
	 * If you need to change the port after the ServerStart is created, use this.
	 *
	 * @param port the port the ServerStart should letch onto
	 */
	void setPort(final int port);

	/**
	 * Adds an {@link ClientConnectedHandler}, that should handle a newly created Client.
	 * <p>
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
	 * <p>
	 * This Distributor is used, if you want to send something to multiple Targets.
	 * <p>
	 * For most other needs, you should use the Session, that is injected into the OnReceive handlers or use the
	 * {@link ClientConnectedHandler}
	 *
	 * @return an internally maintained instance of the Distributor
	 * @see Distributor
	 */
	Distributor distribute();

	/**
	 * Returns a {@link Cache} instance, to set and save specific elements.
	 * <p>
	 * This cache is connected to the Distributor, so that updating and setting objects results in an Distribution to all
	 * Clients that want to be notified about new or updated instances.
	 *
	 * @return an internally maintained instance of the Cache
	 * @see Cache
	 */
	Cache cache();

	/**
	 * Shuts down the Server and disconnects all connected Clients
	 *
	 * @see #softStop()
	 */
	void disconnect();

	/**
	 * Sets the ServerFactory, that is asked to produce the ServerSocket
	 * <p>
	 * If you want to use SSL, you may provide an Factory, that creates an SSLServerSocket.
	 *
	 * @param factory the Factory, that should be used.
	 */
	void setServerSocketFactory(final Factory<Integer, ServerSocket> factory);

	/**
	 * Returns the internally maintained ClientList.
	 * <p>
	 * This may be used, if you need to get a certain Client or apply something to all Clients.
	 * <p>
	 * If you however want to access certain Clients, it is recommended, to create a custom UserObject and set the UserObject,
	 * aggregating the Session of the Client inside of an custom {@link ClientConnectedHandler}. The Client is an real representation
	 * of the Connected PC. Therefor you can do real, irreversible damage at runtime, resulting in an fatal, unrecoverable
	 * error.
	 *
	 * @return the ClientList
	 */
	ClientList clientList();

	/**
	 * Returns the CommunicationRegistration for this ServerStart.
	 * <p>
	 * The internal CommunicationRegistration is unified across al Clients and Connections. If you change this after the
	 * {@link #launch()} call, it is still updated within all Connections.
	 * <p>
	 * This means, if you clear this CommunicationRegistration, it is cleared for all Clients.
	 * <p>
	 * Also, if you {@link CommunicationRegistration#acquire()} and never {@link CommunicationRegistration#release()}, no
	 * Object will be handled by the CommunicationRegistration.
	 *
	 * @return a unified instance of the {@link CommunicationRegistration}
	 */
	CommunicationRegistration getCommunicationRegistration();

	/**
	 * Sets the ExecutorService, to be used internally
	 *
	 * @param executorService the ExecutorService.
	 */
	@Experimental
	void setExecutorService(final ExecutorService executorService);

	/**
	 * Returns an internally maintained {@link RemoteObjectRegistration}.
	 * <p>
	 * This is used, whenever the Client requests a RemoteObject using the {@link com.github.thorbenkuck.netcom2.network.client.RemoteObjectFactory}
	 *
	 * @return a unified instance for the RemoteObjectRegistration
	 * @see com.github.thorbenkuck.netcom2.network.client.RemoteObjectFactory
	 */
	@Experimental
	RemoteObjectRegistration remoteObjects();
}
