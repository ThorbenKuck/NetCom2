package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@APILevel
class PipelineElement<T> implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Consumer<T> consumer;
	private final List<Predicate<T>> predicates = new ArrayList<>();

	@APILevel
	PipelineElement(final Consumer<T> consumer) {
		this.consumer = consumer;
	}

	public void run(final T t) {
		if (test(t)) {
			getConsumer().accept(t);
		}
	}

	public boolean test(final T t) {
		for (final Predicate<T> predicate : predicates) {
			if (! predicate.test(t)) {
				return false;
			}
		}
		return true;
	}

	public Consumer<T> getConsumer() {
		return consumer;
	}

	public void addCondition(final Predicate<T> predicate) {
		predicates.add(predicate);
	}

	@Override
	public boolean equals(final Object obj) {
		return obj != null && obj.getClass().equals(PipelineElement.class) &&
				consumer.equals(((PipelineElement) obj).getConsumer());
	}
}
