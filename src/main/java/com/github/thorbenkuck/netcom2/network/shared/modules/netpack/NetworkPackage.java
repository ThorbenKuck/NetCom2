package com.github.thorbenkuck.netcom2.network.shared.modules.netpack;

import com.github.thorbenkuck.netcom2.interfaces.Factory;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.interfaces.ClientConnectedHandler;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.clients.ConnectionFactory;

import java.net.ServerSocket;
import java.util.Optional;

public interface NetworkPackage {

	Optional<Factory<Integer, ServerSocket>> serverSocketFactory();

	Optional<SocketFactory> socketFactory();

	Optional<ClientConnectedHandler> clientConnectedHandler();

	Optional<ConnectionFactory> connectionFactory();

	void apply(ServerStart serverStart);

	void apply(ClientStart clientStart);
}
