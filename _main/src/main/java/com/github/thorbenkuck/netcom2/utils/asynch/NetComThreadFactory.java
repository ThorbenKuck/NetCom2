package com.github.thorbenkuck.netcom2.utils.asynch;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.utils.NetCom2Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;


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
	private final AtomicBoolean daemon = new AtomicBoolean(true);
	private final List<NetComThread> runningThreadList = new ArrayList<>();
	private final Value<Integer> count = Value.of(0);

	public NetComThreadFactory() {
		logging.instantiated(this);
	}

	private void remove(NetComThread thread) {
		synchronized (runningThreadList) {
			int newCount = count.get() - 1;
			logging.debug("Thread number " + thread.getNumber() + " finished!");
			runningThreadList.remove(thread);
			count.set(newCount);
		}
	}

	private void add(NetComThread thread) {
		synchronized (runningThreadList) {
			int newCount = count.get() + 1;
			logging.debug("Setting thread number to " + newCount);
			thread.setNumber(newCount);
			runningThreadList.add(thread);
			count.set(newCount);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Thread newThread(Runnable runnable) {
		NetCom2Utils.parameterNotNull(runnable);
		logging.trace("Creating new NetComThread");
		NetComThread thread = new NetComThread(runnable);
		logging.trace("Updating daemon state to: " + daemon.get());
		thread.setDaemon(daemon.get());
		logging.trace("Setting finish callback");
		thread.setFinishedCallback(this::remove);
		logging.trace("Storing NetComThread ..");
		add(thread);
		logging.debug("Created new Thread: " + thread);
		return thread;
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

	public List<Thread> getRunningThreads() {
		final List<Thread> copy;
		synchronized (runningThreadList) {
			copy = new ArrayList<>(runningThreadList);
		}

		return copy;
	}
}
