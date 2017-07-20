package com.github.thorbenkuck.netcom2.network.shared;

import java.util.function.Predicate;

public interface PipelineCondition<T> {

	PipelineCondition<T> withRequirement(Predicate<T> predicate);

}
