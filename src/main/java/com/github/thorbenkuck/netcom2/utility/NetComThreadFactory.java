package com.github.thorbenkuck.netcom2.utility;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

public class NetComThreadFactory implements ThreadFactory {

	static final String NET_COM_THREAD_NAME = "NetComThread";
	private final Logging logging = Logging.unified();
	private final NetComThreadContainer threadContainer = new NetComThreadContainer();
	private final NetComThreadGroup threadGroup = new NetComThreadGroup(Thread.currentThread().getThreadGroup(), "NetCom2ThreadGroup");

	public NetComThreadFactory() {
	}

	@Override
	public Thread newThread(Runnable r) {
		NetComThread thread = new NetComThread(threadGroup, r);
		thread.setNetComThreadContainer(threadContainer);
		thread.setPriority(7);
		thread.setDaemon(false);
		thread.setName(NET_COM_THREAD_NAME);
		thread.setUncaughtExceptionHandler((encounteredThread, throwable) -> logging.error("Unhandled Exception on NetComThread!", throwable));
		return thread;
	}

	public void onThreadFinished(Consumer<Thread> threadConsumer) {
		threadContainer.addThreadFinishedConsumer(threadConsumer);
	}
}
