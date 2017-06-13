package de.thorbenkuck.netcom2.network.shared;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class QueuedPipeline<T> extends AbstractPipeline<T, Queue<PipelineElement<T>>> {

	public QueuedPipeline() {
		super(new LinkedList<>());
	}

	@Override
	public PipelineCondition<T> addLast(Consumer<T> consumer) {
		PipelineElement<T> pipelineElement = new PipelineElement<>(consumer);
		try {
			lock();
			getCollection().add(pipelineElement);
		} finally {
			unlock();
		}
		return new PipelineConditionImpl<>(pipelineElement);
	}

	@Override
	public PipelineCondition<T> addFirst(Consumer<T> consumer) {
		Queue<PipelineElement<T>> temp = new LinkedList<>(getCollection());
		PipelineElement<T> pipelineElement = new PipelineElement<>(consumer);
		try {
			lock();
			clear();
			getCollection().add(pipelineElement);
			getCollection().addAll(temp);
		} finally {
			unlock();
		}
		return new PipelineConditionImpl<>(pipelineElement);
	}
}
