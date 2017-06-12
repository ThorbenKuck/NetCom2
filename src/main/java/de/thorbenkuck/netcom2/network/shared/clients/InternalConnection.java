package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.network.shared.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

public interface InternalConnection extends Connection {

	InetAddress getInetAddress();

	InputStream getInputStream() throws IOException;

	OutputStream getOutputStream() throws IOException;

	void setSession(Session session);
}
