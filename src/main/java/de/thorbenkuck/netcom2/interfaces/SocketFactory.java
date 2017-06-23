package de.thorbenkuck.netcom2.interfaces;

import java.io.IOException;
import java.net.Socket;

public interface SocketFactory {

	Socket create(int port, String address) throws IOException;

}
