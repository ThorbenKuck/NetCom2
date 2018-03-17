package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.util.function.Consumer;

public class ThreadedHeartBeat<T> implements HeartBeat<T> {

	private final HeartBeatConfig<T> heartBeatConfig;
	private HeartBeatCore<T> heartBeatCore;
	private Consumer<T> consumer;
	private boolean changed;

	public ThreadedHeartBeat() {
		this(new HeartBeatConfig<>());
	}

	@APILevel
	ThreadedHeartBeat(HeartBeatConfig<T> heartBeatConfig) {
		this.heartBeatConfig = heartBeatConfig;
	}

	private void setup() {
		if (changed) {
			synchronized (heartBeatConfig) {
				heartBeatCore = new HeartBeatCore<>(heartBeatConfig.getActivePredicates(),
						heartBeatConfig.getRunningPredicates(),
						heartBeatConfig.getTimes(),
						heartBeatConfig.getDelay(),
						heartBeatConfig.getTimeUnit());

				changed = false;
				heartBeatConfig.clear();
				heartBeatConfig.unsetChanged();
			}
		}
	}

	@Override
	public HeartBeatConfiguration<T> configure() {
		return new InternalHeartBeatConfiguration<>(this);
	}

	@Override
	public HeartBeatParallel<T> parallel() {
		setup();
		return new InternalHeartBeatParallel<>(heartBeatCore, consumer);
	}

	@Override
	public void run(T t) {
		setup();
		heartBeatCore.setup(t, consumer);
		heartBeatCore.run();
	}

	@Override
	public void run(T t, Consumer<T> consumer) {
		setConsumer(consumer);
		run(t);
	}

	@Override
	public void stop() {
		heartBeatCore.shutdown();
	}

	HeartBeatConfig<T> getHeartBeatConfig() {
		return heartBeatConfig;
	}

	void setConsumer(Consumer<T> consumer) {
		changed = true;
		this.consumer = consumer;
	}
}
