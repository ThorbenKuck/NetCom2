package de.thorbenkuck.netcom2.network.shared.heartbeat;

public interface HeartBeatFactory {

	static HeartBeatFactory get() {
		return new HeartBeatFactoryImpl();
	}

	<T> HeartBeat<T> produce();

}
