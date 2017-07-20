package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.network.interfaces.Loggable;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.CallBack;
import com.github.thorbenkuck.netcom2.network.shared.PipelineCondition;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public interface Connection extends Loggable {

	void close() throws IOException;

	void setup();

	void removeOnDisconnectedConsumer(Consumer<Connection> consumer);

	void write(Object object);

	void addObjectSendListener(CallBack<Object> callBack);

	void addObjectReceivedListener(CallBack<Object> callBack);

	void setThreadPool(ExecutorService executorService);

	Awaiting startListening();

	PipelineCondition<Connection> addOnDisconnectedConsumer(Consumer<Connection> consumer);

	InputStream getInputStream() throws IOException;

	OutputStream getOutputStream() throws IOException;

	BlockingQueue<Object> getSendInterface();

	Session getSession();

	void setSession(Session session);

	String getFormattedAddress();

	int getPort();

	InetAddress getInetAddress();

	boolean isActive();

	Class<?> getKey();

	void setKey(Class<?> connectionKey);
}
