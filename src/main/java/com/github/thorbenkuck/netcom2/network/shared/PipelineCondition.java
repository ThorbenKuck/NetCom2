package com.github.thorbenkuck.netcom2.network.shared;

import java.io.Serializable;
import java.util.function.Predicate;

public interface PipelineCondition<T> extends Serializable {

	PipelineCondition<T> withRequirement(final Predicate<T> predicate);

}
