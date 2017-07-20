package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

class InternalHeartBeatChain<T> implements HeartBeatChain<T> {
	private ThreadedHeartBeat<T> heartBeat;

	InternalHeartBeatChain(ThreadedHeartBeat<T> heartBeat) {
		this.heartBeat = heartBeat;
	}

	@Override
	public HeartBeatConfiguration<T> and() {
		return new InternalHeartBeatConfiguration<>(heartBeat);
	}

	@Override
	public HeartBeat<T> then() {
		return heartBeat;
	}
}
