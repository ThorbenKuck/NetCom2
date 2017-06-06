package de.thorbenkuck.netcom2.network.shared.heartbeat;

public class HeartBeatFactoryImpl implements HeartBeatFactory {

	public <T> HeartBeat<T> produce() {
		return new ThreadedHeartBeat<>();
	}
}
