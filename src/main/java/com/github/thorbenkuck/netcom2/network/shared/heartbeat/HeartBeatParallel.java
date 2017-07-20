package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import java.util.function.Consumer;

public interface HeartBeatParallel<T> {

	Thread run(T t);

	Thread run(T t, Consumer<T> consumer);
}
