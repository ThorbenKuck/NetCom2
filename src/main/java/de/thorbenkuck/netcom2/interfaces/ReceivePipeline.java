package de.thorbenkuck.netcom2.interfaces;

import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import de.thorbenkuck.netcom2.pipeline.ReceivePipelineCondition;

import java.util.function.Consumer;

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

	/**
	 * <p>
	 * Sets the ReceivePipeline to an unchangeable open-state. If you close and than seal it, it cannot be opened any more.
	 * This seal is permanent and makes the ReceivePipeline immutable.
	 * </p>
	 * <p>
	 * <b>Note:</b>  If you seal an Pipeline, it will not get collected, by a {@link CommunicationRegistration#clearAllEmptyPipelines()} call. How ever, a {@link CommunicationRegistration#unRegister(Class)} call will still remove the Pipeline
	 * </p>
	 */
	void seal();

	boolean isSealed();

	void open();

	boolean isClosed();

	void ifClosed(Consumer<ReceivePipeline<T>> consumer);

	void ifClosed(Runnable runnable);

	boolean isEmpty();
}
