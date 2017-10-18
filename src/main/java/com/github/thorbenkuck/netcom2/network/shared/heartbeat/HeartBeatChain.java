package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

public interface HeartBeatChain<T> {
	HeartBeatConfiguration<T> and();

	HeartBeat<T> then();
}
