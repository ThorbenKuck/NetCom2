package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import com.github.thorbenkuck.netcom2.annotations.Tested;

@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.test.HeartBeatTest")
public interface HeartBeat<T> extends HeartBeatRunner<T> {

	static HeartBeat<Object> getNew() {
		return new ThreadedHeartBeat<>();
	}

	HeartBeatConfiguration<T> configure();

	HeartBeatParallel<T> parallel();
}
