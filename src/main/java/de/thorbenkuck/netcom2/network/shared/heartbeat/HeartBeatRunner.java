package de.thorbenkuck.netcom2.network.shared.heartbeat;

import java.util.function.Consumer;

public interface HeartBeatRunner<T> {

	void run(T t);

	void run(T t, Consumer<T> consumer);

	void stop();
}
