package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;

@Synchronized
public class HeartBeatFactoryImpl implements HeartBeatFactory {

	public <T> HeartBeat<T> produce() {
		return new ThreadedHeartBeat<>();
	}
}
