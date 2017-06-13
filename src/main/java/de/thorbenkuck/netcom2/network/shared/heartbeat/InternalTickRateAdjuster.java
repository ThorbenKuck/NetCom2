package de.thorbenkuck.netcom2.network.shared.heartbeat;

class InternalTickRateAdjuster<T> implements TickRateAdjuster<T> {

	private final ThreadedHeartBeat<T> heartBeat;

	InternalTickRateAdjuster(ThreadedHeartBeat<T> heartBeat) {
		this.heartBeat = heartBeat;
	}

	@Override
	public HeartBeatTickInterval<T> times(int i) {
		heartBeat.getHeartBeatConfig().setTimes(i);
		return new InternalHeartBeatTickInterval<>(heartBeat);
	}
}
