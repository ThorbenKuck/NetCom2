package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import java.util.concurrent.TimeUnit;

public interface HeartBeatTickInterval<T> {

	HeartBeatChain<T> in(long time, TimeUnit timeUnit);

}
