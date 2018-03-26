package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * A condition for a ReceivePipeline, which adds the requirements to the underlying pipeline.
 *
 * @param <T> The type
 *
 * @since 1.0
 * @version 1.0
 */
public interface ReceivePipelineCondition<T> {

	/**
	 * Get an empty condition.
	 *
	 * @param <T> The type
	 * @return An empty condition
	 */
	static <T> ReceivePipelineCondition<T> empty() {
		return new EmptyReceivePipelineCondition<>();
	}

	/**
	 * Adds a BiPredicate to the underlying receive pipeline.
	 *
	 * @param userPredicate The bi predicate to add
	 */
	void withRequirement(final BiPredicate<Session, T> userPredicate);

	/**
	 * Adds a predicate to the underlying receive pipeline.
	 *
	 * @param userPredicate The predicate
	 */
	void withRequirement(final Predicate<Session> userPredicate);
}
