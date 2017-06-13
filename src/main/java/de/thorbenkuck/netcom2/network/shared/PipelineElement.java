package de.thorbenkuck.netcom2.network.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

class PipelineElement<T> {

	private final Consumer<T> consumer;
	private final List<Predicate<T>> predicates = new ArrayList<>();

	public PipelineElement(Consumer<T> consumer) {
		this.consumer = consumer;
	}

	public void run(T t) {
		if (test(t)) {
			getConsumer().accept(t);
		}
	}

	public boolean test(T t) {
		for (Predicate<T> predicate : predicates) {
			if (! predicate.test(t)) {
				return false;
			}
		}
		return true;
	}

	public Consumer<T> getConsumer() {
		return consumer;
	}

	public void addCondition(Predicate<T> predicate) {
		predicates.add(predicate);
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass().equals(PipelineElement.class) && consumer.equals(((PipelineElement) obj).getConsumer());
	}
}
