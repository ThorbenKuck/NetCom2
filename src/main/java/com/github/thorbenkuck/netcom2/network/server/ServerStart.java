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

	static ServerStart at(final int port) {
		return new ServerStartImpl(new ServerConnector(port));
	}

	void acceptAllNextClients() throws ClientConnectionFailedException;

	void setPort(final int port);

	void acceptNextClient() throws ClientConnectionFailedException;

	void addClientConnectedHandler(final ClientConnectedHandler clientConnectedHandler);

	void removeClientConnectedHandler(final ClientConnectedHandler clientConnectedHandler);

	Distributor distribute();

	Cache cache();

	void disconnect();

	void setServerSocketFactory(final Factory<Integer, ServerSocket> factory);

	ClientList clientList();

	CommunicationRegistration getCommunicationRegistration();

	void setExecutorService(final ExecutorService executorService);

	@Experimental
	RemoteObjectRegistration remoteObjects();
}
