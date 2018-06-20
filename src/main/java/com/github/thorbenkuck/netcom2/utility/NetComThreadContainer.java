package com.github.thorbenkuck.netcom2.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A container for threads that grants the ability to get notified about finishing threads.
 *
 * @version 1.0
 * @since 1.0
 */
@Deprecated
public class NetComThreadContainer {

	private final List<Thread> netComThreads = new ArrayList<>();
	private final List<Consumer<Thread>> threadFinishedConsumers = new ArrayList<>();

	/**
	 * Notifies all consumers about the specified thread.
	 *
	 * @param thread The finished thread
	 */
	private void notifyAbout(Thread thread) {
		NetCom2Utils.parameterNotNull(thread);
		final List<Consumer<Thread>> consumerCopy;
		synchronized (threadFinishedConsumers) {
			consumerCopy = new ArrayList<>(threadFinishedConsumers);
		}

		consumerCopy.forEach(consumer -> consumer.accept(thread));
	}

	/**
	 * Add a thread to the thread container
	 *
	 * @param thread The thread to add
	 */
	public void addThread(Thread thread) {
		NetCom2Utils.parameterNotNull(thread);
		synchronized (netComThreads) {
			netComThreads.add(thread);
		}
	}

	/**
	 * Removes the specified thread and notifies the listeners
	 *
	 * @param thread The thread to remove
	 */
	public void removeThread(Thread thread) {
		NetCom2Utils.parameterNotNull(thread);
		synchronized (netComThreads) {
			netComThreads.remove(thread);
		}

		notifyAbout(thread);
	}

	/**
	 * Adds a listener for finished threads.
	 *
	 * @param consumer The listener to add
	 */
	public void addThreadFinishedConsumer(Consumer<Thread> consumer) {
		NetCom2Utils.parameterNotNull(consumer);
		synchronized (threadFinishedConsumers) {
			threadFinishedConsumers.add(consumer);
		}
	}
}