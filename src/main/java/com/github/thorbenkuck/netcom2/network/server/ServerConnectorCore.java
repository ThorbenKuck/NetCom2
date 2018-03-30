package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public interface ServerConnectorCore {

	void apply(ServerSocket serverSocket, final Consumer<Socket> socketConsumer) throws ClientConnectionFailedException;

}
