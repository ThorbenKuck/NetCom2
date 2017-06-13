package de.thorbenkuck.netcom2.network.shared.heartbeat;

public interface HeartBeat<T> extends HeartBeatRunner<T> {

	static HeartBeat<Object> getNew() {
		return new ThreadedHeartBeat<>();
	}

	HeartBeatConfiguration<T> configure();

	HeartBeatParallel<T> parallel();
}
