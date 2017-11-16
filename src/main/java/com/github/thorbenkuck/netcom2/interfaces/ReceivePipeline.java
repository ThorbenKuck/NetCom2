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

public interface ReceivePipeline<T> extends Mutex {
	ReceivePipelineCondition<T> addLast(final OnReceive<T> pipelineService);

	ReceivePipelineCondition<T> addLast(final OnReceiveSingle<T> pipelineService);

	ReceivePipelineCondition<T> addLast(final OnReceiveTriple<T> pipelineService);

	ReceivePipelineCondition<T> addFirst(final OnReceive<T> pipelineService);

	ReceivePipelineCondition<T> addFirst(final OnReceiveSingle<T> pipelineService);

	ReceivePipelineCondition<T> addFirst(final OnReceiveTriple<T> pipelineService);

	ReceivePipelineCondition<T> addFirstIfNotContained(final OnReceive<T> pipelineService);

	ReceivePipelineCondition<T> addFirstIfNotContained(final OnReceiveSingle<T> pipelineService);

	ReceivePipelineCondition<T> addFirstIfNotContained(final OnReceiveTriple<T> pipelineService);

	ReceivePipelineCondition<T> addLastIfNotContained(final OnReceive<T> pipelineService);

	ReceivePipelineCondition<T> addLastIfNotContained(final OnReceiveSingle<T> pipelineService);

	ReceivePipelineCondition<T> addLastIfNotContained(final OnReceiveTriple<T> pipelineService);

	ReceivePipelineCondition<T> to(final Object object);

	boolean contains(final OnReceiveTriple<T> onReceiveTriple);

	boolean contains(final OnReceive<T> onReceive);

	boolean contains(final OnReceiveSingle<T> onReceiveSingle);

	boolean isSealed();

	boolean isClosed();

	boolean isEmpty();

	void ifClosed(final Consumer<ReceivePipeline<T>> consumer);

	void ifClosed(final Runnable runnable);

	void setReceivePipelineHandlerPolicy(final ReceivePipelineHandlerPolicy receivePipelineHandlerPolicy);

	void remove(final OnReceive<T> pipelineService);

	void clear();

	void run(final Connection connection, final Session session, final T t);

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
