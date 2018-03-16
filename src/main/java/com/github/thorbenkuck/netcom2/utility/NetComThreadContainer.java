package com.github.thorbenkuck.netcom2.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NetComThreadContainer {

	private final List<Thread> netComThreads = new ArrayList<>();
	private final List<Consumer<Thread>> threadFinishedConsumers = new ArrayList<>();

	private void notifyAbout(Thread thread) {
		NetCom2Utils.parameterNotNull(thread);
		final List<Consumer<Thread>> consumerCopy;
		synchronized (threadFinishedConsumers) {
			consumerCopy = new ArrayList<>(threadFinishedConsumers);
		}

		consumerCopy.forEach(consumer -> consumer.accept(thread));
	}

	public void addThread(Thread thread) {
		NetCom2Utils.parameterNotNull(thread);
		synchronized (netComThreads) {
			netComThreads.add(thread);
		}
	}

	public void removeThread(Thread thread) {
		NetCom2Utils.parameterNotNull(thread);
		synchronized (netComThreads) {
			netComThreads.remove(thread);
		}

		notifyAbout(thread);
	}

	public void addThreadFinishedConsumer(Consumer<Thread> consumer) {
		NetCom2Utils.parameterNotNull(consumer);
		synchronized (threadFinishedConsumers) {
			threadFinishedConsumers.add(consumer);
		}
	}
}