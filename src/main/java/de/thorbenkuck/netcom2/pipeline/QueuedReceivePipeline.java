package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.exceptions.PipelineAccessException;
import de.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class QueuedReceivePipeline<T> implements ReceivePipeline<T> {

	private final Queue<PipelineReceiverImpl<T>> core = new LinkedList<>();
	private final Logging logging = Logging.unified();
	private boolean closed = false;
	private boolean sealed = false;

	@Override
	public ReceivePipelineCondition<T> addLast(OnReceive<T> onReceive) {
		return addLast(new OnReceiveWrapper<>(onReceive));
	}

	@Override
	public ReceivePipelineCondition<T> addLast(OnReceiveSingle<T> onReceiveSingle) {
		return addLast(new OnReceiveSingleWrapper<>(onReceiveSingle));
	}

	@Override
	public ReceivePipelineCondition<T> addLast(OnReceiveTriple<T> pipelineService) {
		PipelineReceiverImpl<T> pipelineReceiver = new PipelineReceiverImpl<>(pipelineService);
		ifClosed(() -> falseAdd(pipelineService));
		ifOpen(() -> {
			synchronized (core) {
				ifOpen(() -> core.add(pipelineReceiver));
			}
			pipelineService.onRegistration();
			logging.debug("Registering onReceive: " + pipelineReceiver);
		});
		return new ReceivePipelineConditionImpl<>(pipelineReceiver);
	}

	@Override
	public ReceivePipelineCondition<T> addFirst(OnReceive<T> onReceive) {
		return addFirst(new OnReceiveWrapper<>(onReceive));
	}

	@Override
	public ReceivePipelineCondition<T> addFirst(OnReceiveSingle<T> pipelineService) {
		return addFirst(new OnReceiveSingleWrapper<>(pipelineService));
	}

	@Override
	public ReceivePipelineCondition<T> addFirst(OnReceiveTriple<T> pipelineService) {
		PipelineReceiverImpl<T> pipelineReceiver = new PipelineReceiverImpl<>(pipelineService);

		ifClosed(() -> falseAdd(pipelineService));

		ifOpen(() -> {
			Queue<PipelineReceiverImpl<T>> newCore = new LinkedList<>();
			newCore.add(pipelineReceiver);
			synchronized (core) {
				newCore.addAll(core);
				core.clear();
				core.addAll(newCore);
			}
			pipelineService.onRegistration();
		});

		return new ReceivePipelineConditionImpl<>(pipelineReceiver);
	}

	@Override
	public void remove(OnReceive<T> pipelineService) {
		synchronized (core) {
			core.remove(new PipelineReceiverImpl<>(new OnReceiveWrapper<>(pipelineService)));
			pipelineService.onUnRegistration();
		}
	}

	@Override
	public void clear() {
		synchronized (core) {
			core.clear();
		}
	}

	@SuppressWarnings ("unchecked")
	@Override
	public void run(Connection connection, Session session, Object t) {
		synchronized (core) {
			core.stream()
					.filter(pipelineReceiver -> pipelineReceiver.test(connection, session, (T) t))
					.forEachOrdered(pipelineReceiver -> pipelineReceiver.getOnReceive().accept(connection, session, (T) t));
		}
	}

	@Override
	public void close() {
		if (! sealed) {
			closed = true;
		}
	}

	@Override
	public void seal() {
		sealed = true;
	}

	@Override
	public boolean isSealed() {
		return sealed;
	}

	@Override
	public void open() {
		if (! sealed) {
			closed = false;
		}
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void ifClosed(Consumer<ReceivePipeline<T>> consumer) {
		ifClosed(() -> consumer.accept(this));
	}

	@Override
	public void ifClosed(Runnable runnable) {
		if (closed) {
			runnable.run();
		}
	}

	protected void requiresOpen() {
		if (closed) {
			throw new PipelineAccessException("ReceivePipeline Closed!");
		}
	}

	private void falseAdd(CanBeRegistered canBeRegistered) {
		canBeRegistered.onRegistration();
		canBeRegistered.doOnError();
		canBeRegistered.onUnRegistration();
	}

	private void ifOpen(Runnable runnable) {
		if (! closed) {
			runnable.run();
		}
	}
}
