package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.network.shared.Session;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

class EmptyReceivePipelineCondition<T> implements ReceivePipelineCondition<T> {
	@Override
	public void withRequirement(BiPredicate<Session, T> userPredicate) {
	}

	@Override
	public void withRequirement(Predicate<Session> userPredicate) {
	}
}
