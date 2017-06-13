package de.thorbenkuck.netcom2.network.shared.heartbeat;

import java.util.function.Consumer;

class InternalHeartBeatParallel<T> implements HeartBeatParallel<T> {

	private final HeartBeatCore<T> heartBeatCore;
	private Consumer<T> consumer;
	private Thread thread;

	InternalHeartBeatParallel(HeartBeatCore<T> heartBeatCore, Consumer<T> consumer) {
		this.heartBeatCore = heartBeatCore;
		this.consumer = consumer;
	}

	@Override
	public Thread run(T t) {
		heartBeatCore.setup(t, consumer);
		thread = new Thread(heartBeatCore);
		thread.start();
		return thread;
	}

	@Override
	public Thread run(T t, Consumer<T> consumer) {
		this.consumer = consumer;
		return run(t);
	}
}
