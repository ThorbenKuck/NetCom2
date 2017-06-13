package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.network.shared.Session;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface ReceivePipelineCondition<T> {

	void withRequirement(BiPredicate<Session, T> userPredicate);

	void withRequirement(Predicate<Session> userPredicate);

}
