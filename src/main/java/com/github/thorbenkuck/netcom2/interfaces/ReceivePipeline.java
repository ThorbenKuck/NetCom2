package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineCondition;
import com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineHandlerPolicy;

import java.util.function.Consumer;

public interface ReceivePipeline<T> {
	ReceivePipelineCondition<T> addLast(OnReceive<T> pipelineService);

	ReceivePipelineCondition<T> addLast(OnReceiveSingle<T> pipelineService);

	ReceivePipelineCondition<T> addLast(OnReceiveTriple<T> pipelineService);

	ReceivePipelineCondition<T> addFirst(OnReceive<T> pipelineService);

	ReceivePipelineCondition<T> addFirst(OnReceiveSingle<T> pipelineService);

	ReceivePipelineCondition<T> addFirst(OnReceiveTriple<T> pipelineService);

	ReceivePipelineCondition<T> addFirstIfNotContained(OnReceive<T> pipelineService);

	ReceivePipelineCondition<T> addFirstIfNotContained(OnReceiveSingle<T> pipelineService);

	ReceivePipelineCondition<T> addFirstIfNotContained(OnReceiveTriple<T> pipelineService);

	ReceivePipelineCondition<T> addLastIfNotContained(OnReceive<T> pipelineService);

	ReceivePipelineCondition<T> addLastIfNotContained(OnReceiveSingle<T> pipelineService);

	ReceivePipelineCondition<T> addLastIfNotContained(OnReceiveTriple<T> pipelineService);

	ReceivePipelineCondition<T> to(Object object);

	boolean contains(OnReceiveTriple<T> onReceiveTriple);

	boolean contains(OnReceive<T> onReceive);

	boolean contains(OnReceiveSingle<T> onReceiveSingle);

	boolean isSealed();

	boolean isClosed();

	boolean isEmpty();

	void ifClosed(Consumer<ReceivePipeline<T>> consumer);

	void ifClosed(Runnable runnable);

	void setReceivePipelineHandlerPolicy(ReceivePipelineHandlerPolicy receivePipelineHandlerPolicy);

	void remove(OnReceive<T> pipelineService);

	void clear();

	void run(Connection connection, Session session, T t);

	void close();

	/**
	 * <p>
	 * Sets the ReceivePipeline to an unchangeable open-state. If you close and than seal it, it cannot be opened any more.
	 * This seal is permanent and makes the ReceivePipeline immutable.
	 * </p>
	 * <p>
	 * <b>Note:</b>  If you seal an Pipeline, it will not get collected, by a {@link CommunicationRegistration#clearAllEmptyPipelines()} call. How ever, a {@link CommunicationRegistration#unRegister(Class)} call will still isRemovable the Pipeline
	 * </p>
	 */
	void seal();

	void open();
}
