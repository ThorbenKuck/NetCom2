package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.interfaces.Pipeline;
import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;

import java.util.LinkedList;
import java.util.Queue;

public class QueuedPipeline<T> implements Pipeline<T> {

	private final Queue<PipelineReceiverImpl<T>> core = new LinkedList<>();
	private boolean closed = false;

	@Override
	public PipelineCondition<T> addLast(OnReceive<T> onReceive) {
		PipelineReceiverImpl<T> pipelineReceiver = new PipelineReceiverImpl<>(onReceive);
		synchronized (core) {
			core.add(pipelineReceiver);
		}
		onReceive.onRegistration();
		return new PipelineConditionImpl<>(pipelineReceiver);
	}

	@Override
	public PipelineCondition<T> addFirst(OnReceive<T> onReceive) {
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
		return new PipelineConditionImpl<>(pipelineReceiver);
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
	public void run(User user, Object t) {
		synchronized (core) {
			checkClosed();
			core.forEach(pipelineReceiver -> {
				if (pipelineReceiver.test(user)) {
					pipelineReceiver.getOnReceive().run(user, (T) t);
				}
			});
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
			throw new RuntimeException("Cannot access a closed Pipeline!");
		}
	}
}
