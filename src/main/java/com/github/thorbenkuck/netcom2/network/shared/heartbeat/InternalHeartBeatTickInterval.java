package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.util.concurrent.TimeUnit;

@APILevel
class InternalHeartBeatTickInterval<T> implements HeartBeatTickInterval<T> {
	private final ThreadedHeartBeat<T> heartBeat;

	@APILevel
	InternalHeartBeatTickInterval(ThreadedHeartBeat<T> heartBeat) {
		this.heartBeat = heartBeat;
	}

	@Override
	public HeartBeatChain<T> in(long time, TimeUnit timeUnit) {
		heartBeat.getHeartBeatConfig().setDelay(time);
		heartBeat.getHeartBeatConfig().setTimeUnit(timeUnit);
		return new InternalHeartBeatChain<>(heartBeat);
	}
}
