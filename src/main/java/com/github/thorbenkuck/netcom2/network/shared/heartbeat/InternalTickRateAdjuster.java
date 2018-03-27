package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;

@APILevel
@Synchronized
class InternalTickRateAdjuster<T> implements TickRateAdjuster<T> {

	private final ThreadedHeartBeat<T> heartBeat;

	@APILevel
	InternalTickRateAdjuster(ThreadedHeartBeat<T> heartBeat) {
		this.heartBeat = heartBeat;
	}

	@Override
	public HeartBeatTickInterval<T> times(int i) {
		heartBeat.getHeartBeatConfig().setTimes(i);
		return new InternalHeartBeatTickInterval<>(heartBeat);
	}
}
