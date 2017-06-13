package de.thorbenkuck.netcom2.network.shared.heartbeat;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface RunAdjuster<T> {
	HeartBeatChain<T> until(Predicate<T> predicate);

	HeartBeatChain<T> until(BooleanSupplier booleanSupplier);

	HeartBeatChain<T> onlyIf(Predicate<T> predicate);

	HeartBeatChain<T> onlyIf(BooleanSupplier booleanSupplier);

	HeartBeatChain<T> setAction(Consumer<T> consumer);
}
