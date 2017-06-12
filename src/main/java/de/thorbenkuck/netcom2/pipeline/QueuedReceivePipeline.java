package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;

import java.util.LinkedList;
import java.util.Queue;

public class QueuedReceivePipeline<T> implements ReceivePipeline<T> {

	private final Queue<PipelineReceiverImpl<T>> core = new LinkedList<>();
	private boolean closed = false;

	@Override
	public ReceivePipelineCondition<T> addLast(OnReceive<T> onReceive) {
		return addLast((OnReceiveTriple<T>) onReceive);
	}

	@Override
	public ReceivePipelineCondition<T> addLast(OnReceiveSingle<T> pipelineService) {
		return addLast((OnReceive<T>) pipelineService);
	}

	@Override
	public ReceivePipelineCondition<T> addLast(OnReceiveTriple<T> pipelineService) {
		PipelineReceiverImpl<T> pipelineReceiver = new PipelineReceiverImpl<>(pipelineService);
		synchronized (core) {
			core.add(pipelineReceiver);
		}
		pipelineService.onRegistration();
		return new ReceivePipelineConditionImpl<>(pipelineReceiver);
	}

	@Override
	public ReceivePipelineCondition<T> addFirst(OnReceive<T> onReceive) {
		return addFirst((OnReceiveTriple<T>) onReceive);
	}

	@Override
	public ReceivePipelineCondition<T> addFirst(OnReceiveSingle<T> pipelineService) {
		return addFirst((OnReceive<T>) pipelineService);
	}

	@Override
	public ReceivePipelineCondition<T> addFirst(OnReceiveTriple<T> pipelineService) {
		Queue<PipelineReceiverImpl<T>> newCore = new LinkedList<>();
		PipelineReceiverImpl<T> pipelineReceiver = new PipelineReceiverImpl<>(pipelineService);
		newCore.add(pipelineReceiver);
		synchronized (core) {
			checkClosed();
			newCore.addAll(core);
			core.clear();
			core.addAll(newCore);
		}
		pipelineService.onRegistration();
		return new ReceivePipelineConditionImpl<>(pipelineReceiver);
	}

	@Override
	public void remove(OnReceive<T> pipelineService) {
		synchronized (core) {
			checkClosed();
			core.remove(new PipelineReceiverImpl<>(pipelineService));
		}
	}

	@Override
	public void clear() {
		synchronized (core) {
			checkClosed();
			core.clear();
		}
	}

	@Override
	public void run(Connection connection, Session session, Object t) {
		synchronized (core) {
			checkClosed();
			core.stream()
					.filter(pipelineReceiver -> pipelineReceiver.test(connection, session, (T) t))
					.forEachOrdered(pipelineReceiver -> pipelineReceiver.getOnReceive().accept(connection, session, (T) t));
		}
	}

	@Override
	public void close() {
		closed = true;
	}

	@Override
	public void open() {
		closed = false;
	}

	private void checkClosed() {
		if (closed) {
			throw new RuntimeException("Cannot getDefault a closed ReceivePipeline!");
		}
	}
}
