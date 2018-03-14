package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

@APILevel
class InternalHeartBeatChain<T> implements HeartBeatChain<T> {
	private ThreadedHeartBeat<T> heartBeat;

	@APILevel
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
