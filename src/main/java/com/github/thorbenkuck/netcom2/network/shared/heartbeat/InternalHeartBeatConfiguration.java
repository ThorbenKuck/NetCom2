package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

class InternalHeartBeatConfiguration<T> implements HeartBeatConfiguration<T> {

	private ThreadedHeartBeat<T> heartBeat;

	InternalHeartBeatConfiguration(ThreadedHeartBeat<T> heartBeat) {
		this.heartBeat = heartBeat;
	}

	@Override
	public TickRateAdjuster<T> tickRate() {
		return new InternalTickRateAdjuster<>(heartBeat);
	}

	@Override
	public RunAdjuster<T> run() {
		return new InternalRunAdjuster<>(heartBeat);
	}
}
