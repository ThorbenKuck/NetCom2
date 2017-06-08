package de.thorbenkuck.netcom2.network.shared.heartbeat;

public interface HeartBeatFactory {

	HeartBeatFactory heartBeatFactory = new HeartBeatFactoryImpl();

	static HeartBeatFactory get() {
		return heartBeatFactory;
	}

	<T> HeartBeat<T> produce();

}
