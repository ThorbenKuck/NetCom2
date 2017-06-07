package de.thorbenkuck.netcom2.interfaces;

import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.pipeline.ReceivePipelineCondition;

public interface ReceivePipeline<T> {
	ReceivePipelineCondition<T> addLast(OnReceive<T> pipelineService);

	ReceivePipelineCondition<T> addFirst(OnReceive<T> pipelineService);

	void remove(OnReceive<T> pipelineService);

	void clear();

	void run(Session session, Object e);

	void close();

	void open();
}
