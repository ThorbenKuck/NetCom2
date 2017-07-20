package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface ReceivePipelineCondition<T> {

	static <T> ReceivePipelineCondition<T> empty() {
		return new EmptyReceivePipelineCondition<>();
	}

	void withRequirement(BiPredicate<Session, T> userPredicate);

	void withRequirement(Predicate<Session> userPredicate);
}
