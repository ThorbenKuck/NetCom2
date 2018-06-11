package com.github.thorbenkuck.netcom2.utility;

import com.github.thorbenkuck.netcom2.logging.Logging;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;


/**
 * This ThreadFactory is primarily used to give to ExecutorServices in order to generate NetCom2's own Threads.
 * <p>
 * It allows to change whether newly spawned Threads should be daemon or not.
 *
 * @version 1.0
 * @since 1.0
 */
public class NetComThreadFactory implements ThreadFactory {

	static final String NET_COM_THREAD_NAME = "NetComThread";
	private final Logging logging = Logging.unified();
	private final NetComThreadContainer threadContainer = new NetComThreadContainer();
	// Check whether or not the Thread-Group helps
	// or hinders the NetComThread (thread-safety)
	private final NetComThreadGroup threadGroup = new NetComThreadGroup(Thread.currentThread().getThreadGroup(), "NetCom2ThreadGroup");
	private final AtomicBoolean daemon = new AtomicBoolean(true);

	NetComThreadFactory() {
		logging.debug("Instantiated new NetComThreadFactory");
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * Add the specified Consumer to the internal ThreadContainer, if it isn't null.
	 *
	 * @param threadConsumer The Consumer to add
	 */
	public void onThreadFinished(Consumer<Thread> threadConsumer) {
		NetCom2Utils.parameterNotNull(threadConsumer);
		threadContainer.addThreadFinishedConsumer(threadConsumer);
	}

	/**
	 * Sets whether newly spawned Thread instances should be daemon or not.
	 *
	 * @param daemon The daemon state
	 */
	public void setDaemon(boolean daemon) {
		logging.debug("Setting daemon state of all Threads to: " + daemon);
		this.daemon.set(daemon);
	}
}
