package com.github.thorbenkuck.netcom2.network.shared;

import java.util.LinkedList;
import java.util.Queue;

public class QueuedPipeline<T> extends AbstractPipeline<T, Queue<PipelineElement<T>>> {

	private static final long serialVersionUID = 4414647424220391756L;
	public QueuedPipeline() {
		super(new LinkedList<>());
	}
}
