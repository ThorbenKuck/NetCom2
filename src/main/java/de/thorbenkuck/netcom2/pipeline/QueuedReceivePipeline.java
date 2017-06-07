package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;

import java.util.LinkedList;
import java.util.Queue;

public class QueuedReceivePipeline<T> implements ReceivePipeline<T> {

	private final Queue<PipelineReceiverImpl<T>> core = new LinkedList<>();
	private boolean closed = false;

	@Override
	public ReceivePipelineCondition<T> addLast(OnReceive<T> onReceive) {
		PipelineReceiverImpl<T> pipelineReceiver = new PipelineReceiverImpl<>(onReceive);
		synchronized (core) {
			core.add(pipelineReceiver);
		}
		onReceive.onRegistration();
		return new ReceivePipelineConditionImpl<>(pipelineReceiver);
	}

	@Override
	public ReceivePipelineCondition<T> addLast(OnReceiveSingle<T> pipelineService) {
		return addLast((OnReceive<T>) pipelineService);
	}

	@Override
	public ReceivePipelineCondition<T> addFirst(OnReceive<T> onReceive) {
		Queue<PipelineReceiverImpl<T>> newCore = new LinkedList<>();
		PipelineReceiverImpl<T> pipelineReceiver = new PipelineReceiverImpl<>(onReceive);
		newCore.add(pipelineReceiver);
		synchronized (core) {
			checkClosed();
			newCore.addAll(core);
			core.clear();
			core.addAll(newCore);
		}
		onReceive.onRegistration();
		return new ReceivePipelineConditionImpl<>(pipelineReceiver);
	}

	@Override
	public ReceivePipelineCondition<T> addFirst(OnReceiveSingle<T> pipelineService) {
		return addFirst((OnReceive<T>) pipelineService);
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
	public void run(Session session, Object t) {
		synchronized (core) {
			checkClosed();
			core.stream()
					.filter(PipelineReceiver -> PipelineReceiver.test(session, (T) t))
					.forEachOrdered(pipelineReceiver -> pipelineReceiver.getOnReceive().accept(session, (T) t));
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
