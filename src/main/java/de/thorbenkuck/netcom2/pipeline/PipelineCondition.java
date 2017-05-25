package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.network.shared.Session;

import java.util.function.Predicate;

public interface PipelineCondition<T> {

	void withRequirement(Predicate<Session> userPredicate);
}
