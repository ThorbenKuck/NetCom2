package de.thorbenkuck.netcom2.interfaces;

import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import de.thorbenkuck.netcom2.pipeline.ReceivePipelineCondition;

public interface ReceivePipeline<T> {
	ReceivePipelineCondition<T> addLast(OnReceive<T> pipelineService);

	ReceivePipelineCondition<T> addLast(OnReceiveSingle<T> pipelineService);

	ReceivePipelineCondition<T> addLast(OnReceiveTriple<T> pipelineService);

	ReceivePipelineCondition<T> addFirst(OnReceive<T> pipelineService);

	ReceivePipelineCondition<T> addFirst(OnReceiveSingle<T> pipelineService);

	ReceivePipelineCondition<T> addFirst(OnReceiveTriple<T> pipelineService);

	void remove(OnReceive<T> pipelineService);

	void clear();

	void run(Connection connection, Session session, Object t);

	void close();

	void open();
}
