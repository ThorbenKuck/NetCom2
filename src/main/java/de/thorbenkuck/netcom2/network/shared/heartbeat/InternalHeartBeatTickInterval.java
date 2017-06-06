package de.thorbenkuck.netcom2.network.shared.heartbeat;

import java.util.concurrent.TimeUnit;

class InternalHeartBeatTickInterval<T> implements HeartBeatTickInterval<T> {
	private final ThreadedHeartBeat<T> heartBeat;

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
