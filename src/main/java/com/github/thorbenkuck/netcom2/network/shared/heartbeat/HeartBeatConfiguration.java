package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

public interface HeartBeatConfiguration<T> {
	TickRateAdjuster<T> tickRate();

	RunAdjuster<T> run();
}
