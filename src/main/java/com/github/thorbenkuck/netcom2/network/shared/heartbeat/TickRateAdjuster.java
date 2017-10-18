package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

public interface TickRateAdjuster<T> {

	HeartBeatTickInterval<T> times(int i);

}
