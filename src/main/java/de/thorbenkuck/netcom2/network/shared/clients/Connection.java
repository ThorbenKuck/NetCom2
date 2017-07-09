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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public interface Connection extends Loggable {

	void close() throws IOException;

	Awaiting startListening();

	Awaiting receivingOfClass(Class clazz);

	PipelineCondition<Connection> addOnDisconnectedConsumer(Consumer<Connection> consumer);

	void removeOnDisconnectedConsumer(Consumer<Connection> consumer);

	void write(Object object);

	void addListener(Feasible<Class> feasible);

	InputStream getInputStream() throws IOException;

	OutputStream getOutputStream() throws IOException;

	LinkedBlockingQueue<Object> getSendInterface();

	Session getSession();

	void setSession(Session session);

	String getFormattedAddress();

	int getPort();

	InetAddress getInetAddress();

	boolean isActive();

	Class<?> getKey();

	void setKey(Class<?> connectionKey);

	void setThreadPool(ExecutorService executorService);
}
