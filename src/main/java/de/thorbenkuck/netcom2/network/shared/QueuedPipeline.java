package de.thorbenkuck.netcom2.network.shared;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class QueuedPipeline<T> extends AbstractPipeline<T, Queue<PipelineElement<T>>> {

	public QueuedPipeline() {
		super(new LinkedList<>());
	}
}
