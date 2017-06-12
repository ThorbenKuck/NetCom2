package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.network.interfaces.Loggable;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.Feasible;
import de.thorbenkuck.netcom2.network.shared.PipelineCondition;
import de.thorbenkuck.netcom2.network.shared.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public interface Connection extends Loggable {

	void close() throws IOException;

	String getFormattedAddress();

	boolean active();

	PipelineCondition<Connection> addOnDisconnectedConsumer(Consumer<Connection> consumer);

	void removeOnDisconnectedConsumer(Consumer<Connection> consumer);

	int getPort();

	InetAddress getInetAddress();

	void addListener(Feasible<Class> feasible);

	Awaiting startListening();

	Awaiting receivingOfClass(Class clazz);

	InputStream getInputStream() throws IOException;

	OutputStream getOutputStream() throws IOException;

	void offerToSend(Object object);

	LinkedBlockingQueue<Object> getSendInterface();

	Session getSession();

	void setSession(Session session);

	// TODO Remove
	Socket getSocket();
}
