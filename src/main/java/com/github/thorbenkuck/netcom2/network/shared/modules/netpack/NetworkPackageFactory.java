package com.github.thorbenkuck.netcom2.network.shared.modules.netpack;

import com.github.thorbenkuck.netcom2.interfaces.Factory;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.interfaces.ClientConnectedHandler;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.clients.ConnectionFactory;

import java.net.ServerSocket;
import java.util.function.Consumer;

public interface NetworkPackageFactory {

	static NetworkPackageFactory access() {
		return new DefaultNetworkPackageFactory();
	}

	NetworkPackageFactory add(SocketFactory socketFactory);

	NetworkPackageFactory add(Factory<Integer, ServerSocket> serverSocketFactory);

	NetworkPackageFactory add(ClientConnectedHandler clientConnectedHandler);

	NetworkPackageFactory add(ConnectionFactory connectionFactory);

	NetworkPackageFactory ifServer(Consumer<ServerStart> consumer);

	NetworkPackageFactory ifClient(Consumer<ClientStart> consumer);

	NetworkPackage build();

}
