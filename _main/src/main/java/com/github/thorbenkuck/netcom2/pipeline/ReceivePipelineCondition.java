package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * A condition for a ReceivePipeline, which adds the requirements to the underlying pipeline.
 *
 * @param <T> The type
 * @version 1.0
 * @since 1.0
 */
public interface ReceivePipelineCondition<T> {

	/**
	 * Get an empty condition.
	 *
	 * @param <T> The type
	 * @return An empty condition
	 */
	static <T> com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineCondition<T> empty() {
		return new com.github.thorbenkuck.netcom2.pipeline.EmptyReceivePipelineCondition<>();
	}

	com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineCondition<T> require(final BiPredicate<Session, T> userPredicate);

	com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineCondition<T> require(final Predicate<Session> userPredicate);
}
