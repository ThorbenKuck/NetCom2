package com.github.thorbenkuck.netcom2.utility;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class NetComThreadFactory implements ThreadFactory {

	static final String NET_COM_THREAD_NAME = "NetComThread";
	private final Logging logging = Logging.unified();
	private final NetComThreadContainer threadContainer = new NetComThreadContainer();
	private final NetComThreadGroup threadGroup = new NetComThreadGroup(Thread.currentThread().getThreadGroup(), "NetCom2ThreadGroup");
	private AtomicBoolean daemon = new AtomicBoolean(true);

	NetComThreadFactory() {
	}

	@Override
	public Thread newThread(Runnable r) {
		NetCom2Utils.parameterNotNull(r);
		NetComThread thread = new NetComThread(threadGroup, r);
		thread.setNetComThreadContainer(threadContainer);
		thread.setDaemon(daemon.get());
		return thread;
	}

	public void onThreadFinished(Consumer<Thread> threadConsumer) {
		NetCom2Utils.parameterNotNull(threadConsumer);
		threadContainer.addThreadFinishedConsumer(threadConsumer);
	}

	public void setDaemon(boolean daemon) {
		this.daemon.set(daemon);
	}
}
