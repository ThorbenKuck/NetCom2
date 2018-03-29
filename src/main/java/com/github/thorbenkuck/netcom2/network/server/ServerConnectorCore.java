package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;

import java.net.ServerSocket;
import java.net.Socket;

public interface ServerConnectorCore {

	Socket apply(ServerSocket serverSocket) throws ClientConnectionFailedException;

}
