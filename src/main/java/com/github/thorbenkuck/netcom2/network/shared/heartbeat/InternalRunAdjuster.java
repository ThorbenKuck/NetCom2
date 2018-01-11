package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

@APILevel
class InternalRunAdjuster<T> implements RunAdjuster<T> {
	private ThreadedHeartBeat<T> heartBeat;

	@APILevel
	InternalRunAdjuster(ThreadedHeartBeat<T> heartBeat) {
		this.heartBeat = heartBeat;
	}

	@Override
	public HeartBeatChain<T> until(Predicate<T> predicate) {
		heartBeat.getHeartBeatConfig().setRunningPredicate(predicate);
		return new InternalHeartBeatChain<>(heartBeat);
	}

	@Override
	public HeartBeatChain<T> until(BooleanSupplier booleanSupplier) {
		heartBeat.getHeartBeatConfig().setRunningPredicate(t -> booleanSupplier.getAsBoolean());
		return new InternalHeartBeatChain<>(heartBeat);
	}


	@Override
	public HeartBeatChain<T> onlyIf(Predicate<T> predicate) {
		heartBeat.getHeartBeatConfig().addActivePredicate(predicate);
		return new InternalHeartBeatChain<>(heartBeat);
	}

	@Override
	public HeartBeatChain<T> onlyIf(BooleanSupplier booleanSupplier) {
		heartBeat.getHeartBeatConfig().addActivePredicate(t -> booleanSupplier.getAsBoolean());
		return new InternalHeartBeatChain<>(heartBeat);
	}

	@Override
	public HeartBeatChain<T> setAction(Consumer<T> consumer) {
		heartBeat.setConsumer(consumer);
		return new InternalHeartBeatChain<>(heartBeat);
	}
}
