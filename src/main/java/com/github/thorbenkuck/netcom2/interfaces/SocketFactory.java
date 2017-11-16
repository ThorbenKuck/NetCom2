package com.github.thorbenkuck.netcom2.interfaces;

import java.io.IOException;
import java.net.Socket;

@FunctionalInterface
public interface SocketFactory {

	Socket create(final int port, final String address) throws IOException;

}
