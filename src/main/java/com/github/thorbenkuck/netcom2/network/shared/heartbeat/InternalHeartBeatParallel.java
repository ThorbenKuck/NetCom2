package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.util.function.Consumer;

@APILevel
class InternalHeartBeatParallel<T> implements HeartBeatParallel<T> {

	private final HeartBeatCore<T> heartBeatCore;
	private Consumer<T> consumer;

	@APILevel
	InternalHeartBeatParallel(final HeartBeatCore<T> heartBeatCore, final Consumer<T> consumer) {
		this.heartBeatCore = heartBeatCore;
		this.consumer = consumer;
	}

	@Override
	public Thread run(final T t) {
		heartBeatCore.setup(t, consumer);
		final Thread thread = new Thread(heartBeatCore);
		thread.start();
		return thread;
	}

	@Override
	public Thread run(final T t, final Consumer<T> consumer) {
		this.consumer = consumer;
		return run(t);
	}
}
