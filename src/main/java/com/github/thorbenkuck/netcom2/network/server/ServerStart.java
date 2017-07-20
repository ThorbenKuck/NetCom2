package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.interfaces.Factory;
import com.github.thorbenkuck.netcom2.interfaces.MultipleConnections;
import com.github.thorbenkuck.netcom2.interfaces.SoftStoppable;
import com.github.thorbenkuck.netcom2.network.handler.ClientConnectedHandler;
import com.github.thorbenkuck.netcom2.network.interfaces.Launch;
import com.github.thorbenkuck.netcom2.network.interfaces.Loggable;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

public interface ServerStart extends Launch, SoftStoppable, Loggable, MultipleConnections {

	static ServerStart at(int port) {
		return new ServerStartImpl(new ServerConnector(port));
	}

	void acceptAllNextClients() throws ClientConnectionFailedException;

	void setPort(int port);

	void acceptNextClient() throws ClientConnectionFailedException;

	void addClientConnectedHandler(ClientConnectedHandler clientConnectedHandler);

	void removeClientConnectedHandler(ClientConnectedHandler clientConnectedHandler);

	Distributor distribute();

	Cache cache();

	void disconnect();

	void setServerSocketFactory(Factory<Integer, ServerSocket> factory);

	ClientList clientList();

	CommunicationRegistration getCommunicationRegistration();

	void setExecutorService(ExecutorService executorService);
}
