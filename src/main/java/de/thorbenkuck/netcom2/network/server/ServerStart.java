package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import de.thorbenkuck.netcom2.interfaces.Factory;
import de.thorbenkuck.netcom2.interfaces.SoftStoppable;
import de.thorbenkuck.netcom2.network.handler.ClientConnectedHandler;
import de.thorbenkuck.netcom2.network.interfaces.Launch;
import de.thorbenkuck.netcom2.network.interfaces.Loggable;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

import java.net.ServerSocket;

public interface ServerStart extends Launch, SoftStoppable, Loggable {

	static ServerStart of(int port) {
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

	void setSocketFactory(Factory<Integer, ServerSocket> factory);

	ClientList clientList();

	CommunicationRegistration getCommunicationRegistration();
}
