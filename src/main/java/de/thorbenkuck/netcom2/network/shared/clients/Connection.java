package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.network.interfaces.Loggable;
import de.thorbenkuck.netcom2.network.shared.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public interface Connection extends Loggable {

	void close() throws IOException;

	void setup();

	void removeOnDisconnectedConsumer(Consumer<Connection> consumer);

	void write(Object object);

	void addObjectSendListener(CallBack<Object> callBack);

	void addObjectReceivedListener(CallBack<Object> callBack);

	void setSession(Session session);

	void setKey(Class<?> connectionKey);

	void setThreadPool(ExecutorService executorService);

	Awaiting startListening();

	PipelineCondition<Connection> addOnDisconnectedConsumer(Consumer<Connection> consumer);

	InputStream getInputStream() throws IOException;

	OutputStream getOutputStream() throws IOException;

	BlockingQueue<Object> getSendInterface();

	Session getSession();

	String getFormattedAddress();

	int getPort();

	InetAddress getInetAddress();

	boolean isActive();

	Class<?> getKey();
}
