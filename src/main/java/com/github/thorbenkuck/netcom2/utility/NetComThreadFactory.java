package com.github.thorbenkuck.netcom2.utility;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class NetComThreadFactory implements ThreadFactory {

	static final String NET_COM_THREAD_NAME = "NetComThread";
	private final Logging logging = Logging.unified();
	private final NetComThreadContainer threadContainer = new NetComThreadContainer();
	// Check whether or not the Thread-Group helps
	// or hinders the NetComThread (thread-safety)
	private final NetComThreadGroup threadGroup = new NetComThreadGroup(Thread.currentThread().getThreadGroup(), "NetCom2ThreadGroup");
	private AtomicBoolean daemon = new AtomicBoolean(true);

	NetComThreadFactory() {
		logging.debug("Instantiated new NetComThreadFactory");
	}

	@Override
	public Thread newThread(Runnable r) {
		NetCom2Utils.parameterNotNull(r);
		logging.trace("Creating new NetComThread");
		NetComThread thread = new NetComThread(threadGroup, r);
		logging.trace("Updating NetComThreadContainer of freshly created Thread");
		thread.setNetComThreadContainer(threadContainer);
		logging.trace("Updating daemon state to: " + daemon.get());
		thread.setDaemon(daemon.get());
		logging.debug("Created new Thread: " + thread);
		return thread;
	}

	public void onThreadFinished(Consumer<Thread> threadConsumer) {
		NetCom2Utils.parameterNotNull(threadConsumer);
		threadContainer.addThreadFinishedConsumer(threadConsumer);
	}

	public void setDaemon(boolean daemon) {
		logging.debug("Setting daemon state of all Threads to: " + daemon);
		this.daemon.set(daemon);
	}
}
