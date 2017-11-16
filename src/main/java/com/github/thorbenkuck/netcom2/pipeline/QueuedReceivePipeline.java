package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.exceptions.PipelineAccessException;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class QueuedReceivePipeline<T> implements ReceivePipeline<T> {

	private final Queue<PipelineReceiverImpl<T>> core = new LinkedList<>();
	private final Logging logging = Logging.unified();
	private final Lock policyLock = new ReentrantLock();
	private final Class<T> clazz;
	private final ReceiveObjectHandlerWrapper receiveObjectHandlerWrapper = new ReceiveObjectHandlerWrapper();
	private final Semaphore semaphore = new Semaphore(1);
	private boolean closed = false;
	private boolean sealed = false;
	private ReceivePipelineHandlerPolicy receivePipelineHandlerPolicy = ReceivePipelineHandlerPolicy.ALLOW_SINGLE;

	public QueuedReceivePipeline(final Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public ReceivePipelineCondition<T> addLast(final OnReceive<T> onReceive) {
		return addLast(new OnReceiveWrapper<>(onReceive));
	}

	@Override
	public ReceivePipelineCondition<T> addLast(final OnReceiveSingle<T> onReceiveSingle) {
		return addLast(new OnReceiveSingleWrapper<>(onReceiveSingle));
	}

	@Override
	public ReceivePipelineCondition<T> addLast(final OnReceiveTriple<T> pipelineService) {
		final PipelineReceiverImpl<T> pipelineReceiver = new PipelineReceiverImpl<>(pipelineService);
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
	public ReceivePipelineCondition<T> addFirst(final OnReceive<T> onReceive) {
		return addFirst(new OnReceiveWrapper<>(onReceive));
	}

	@Override
	public ReceivePipelineCondition<T> addFirst(final OnReceiveSingle<T> pipelineService) {
		return addFirst(new OnReceiveSingleWrapper<>(pipelineService));
	}

	@Override
	public ReceivePipelineCondition<T> addFirst(final OnReceiveTriple<T> pipelineService) {
		final PipelineReceiverImpl<T> pipelineReceiver = new PipelineReceiverImpl<>(pipelineService);

		if (isClosed()) {
			falseAdd(pipelineService);
		} else {
			final Queue<PipelineReceiverImpl<T>> newCore = new LinkedList<>();
			newCore.add(pipelineReceiver);
			synchronized (core) {
				newCore.addAll(core);
				core.clear();
				core.addAll(newCore);
			}
			pipelineService.onRegistration();
		}

		return new ReceivePipelineConditionImpl<>(pipelineReceiver);
	}

	@Override
	public ReceivePipelineCondition<T> addFirstIfNotContained(final OnReceive<T> pipelineService) {
		return addFirstIfNotContained(new OnReceiveWrapper<>(pipelineService));
	}

	@Override
	public ReceivePipelineCondition<T> addFirstIfNotContained(final OnReceiveSingle<T> pipelineService) {
		return addFirstIfNotContained(new OnReceiveSingleWrapper<>(pipelineService));
	}

	@Override
	public ReceivePipelineCondition<T> addFirstIfNotContained(final OnReceiveTriple<T> pipelineService) {
		if (!contains(pipelineService)) {
			return addFirst(pipelineService);
		}
		return ReceivePipelineCondition.empty();
	}

	@Override
	public ReceivePipelineCondition<T> addLastIfNotContained(final OnReceive<T> pipelineService) {
		return addLastIfNotContained(new OnReceiveWrapper<>(pipelineService));
	}

	@Override
	public ReceivePipelineCondition<T> addLastIfNotContained(final OnReceiveSingle<T> pipelineService) {
		return addLastIfNotContained(new OnReceiveSingleWrapper<>(pipelineService));
	}

	@Override
	public ReceivePipelineCondition<T> addLastIfNotContained(final OnReceiveTriple<T> pipelineService) {
		if (!contains(pipelineService)) {
			return addLast(pipelineService);
		}
		return ReceivePipelineCondition.empty();
	}

	@Override
	public ReceivePipelineCondition<T> to(final Object object) {
		requiresOpen();
		requiredNotSealed();
		try {
			policyLock.lock();
			receivePipelineHandlerPolicy.prepare(this);
			final ReceivePipelineCondition<T> toReturn = addFirst(receiveObjectHandlerWrapper.wrap(object, clazz));
			receivePipelineHandlerPolicy.afterAdding(this);
			return toReturn;
		} finally {
			policyLock.unlock();
		}
	}

	protected void requiresOpen() {
		if (closed) {
			throw new PipelineAccessException("ReceivePipeline Closed!");
		}
	}

	protected void requiredNotSealed() {
		if (sealed) {
			throw new PipelineAccessException("ReceivePipeline is sealed!");
		}
	}

	@Override
	public boolean contains(final OnReceiveTriple<T> onReceiveTriple) {
		return core.contains(new PipelineReceiverImpl<>(onReceiveTriple));
	}

	@Override
	public boolean contains(final OnReceive<T> onReceive) {
		return contains(new OnReceiveWrapper<>(onReceive));
	}

	@Override
	public boolean contains(final OnReceiveSingle<T> onReceiveSingle) {
		return contains(new OnReceiveSingleWrapper<>(onReceiveSingle));
	}

	@Override
	public boolean isSealed() {
		return sealed;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public boolean isEmpty() {
		return core.isEmpty();
	}

	@Override
	public void ifClosed(final Consumer<ReceivePipeline<T>> consumer) {
		ifClosed(() -> consumer.accept(this));
	}

	@Override
	public void ifClosed(final Runnable runnable) {
		if (closed) {
			runnable.run();
		}
	}

	@Override
	public void setReceivePipelineHandlerPolicy(final ReceivePipelineHandlerPolicy receivePipelineHandlerPolicy) {
		try {
			policyLock.lock();
			this.receivePipelineHandlerPolicy = receivePipelineHandlerPolicy;
		} finally {
			policyLock.unlock();
		}
	}

	@Override
	public void remove(final OnReceive<T> pipelineService) {
		synchronized (core) {
			core.remove(new PipelineReceiverImpl<>(new OnReceiveWrapper<>(pipelineService)));
			pipelineService.onUnRegistration();
		}
	}

	@Override
	public void clear() {
		if (isClosed()) {
			throw new PipelineAccessException("Cannot clear an closed Pipeline!");
		}
		synchronized (core) {
			core.clear();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run(final Connection connection, final Session session, final T t) {
		try {
			synchronized (core) {
				core.stream()
						.filter(pipelineReceiver -> pipelineReceiver.test(connection, session, t))
						.forEachOrdered(
								pipelineReceiver -> pipelineReceiver.getOnReceive().accept(connection, session, t));
			}
		} catch (final Exception e) {
			logging.error("Encountered exception!", e);
		}
	}

	@Override
	public void close() {
		requiredNotSealed();
		logging.debug("Closing ReceivePipeline for " + clazz);
		closed = true;
	}

	@Override
	public void seal() {
		logging.debug("Sealing ReceivePipeline for " + clazz);
		sealed = true;
	}

	@Override
	public void open() {
		requiredNotSealed();
		logging.debug("Opening ReceivePipeline for " + clazz);
		closed = false;
	}

	private void falseAdd(final CanBeRegistered canBeRegistered) {
		canBeRegistered.onAddFailed();
	}

	private void ifOpen(final Runnable runnable) {
		if (!closed) {
			runnable.run();
		}
	}

	@Override
	public int hashCode() {
		int result = core.hashCode();
		result = 31 * result + logging.hashCode();
		result = 31 * result + policyLock.hashCode();
		result = 31 * result + clazz.hashCode();
		result = 31 * result + receiveObjectHandlerWrapper.hashCode();
		result = 31 * result + (closed ? 1 : 0);
		result = 31 * result + (sealed ? 1 : 0);
		result = 31 * result + receivePipelineHandlerPolicy.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof QueuedReceivePipeline)) return false;

		final QueuedReceivePipeline<?> that = (QueuedReceivePipeline<?>) o;
		try {
			that.acquire();

			if (closed != that.closed) return false;
			if (sealed != that.sealed) return false;
			if (!core.equals(that.core)) return false;
			if (!logging.equals(that.logging)) return false;
			if (!policyLock.equals(that.policyLock)) return false;
			if (!clazz.equals(that.clazz)) return false;
			if (!receiveObjectHandlerWrapper.equals(that.receiveObjectHandlerWrapper))
				return false;
			return receivePipelineHandlerPolicy == that.receivePipelineHandlerPolicy;
		} catch (final InterruptedException e) {
			logging.catching(e);
			return false;
		} finally {
			that.release();
		}
	}

	@Override
	public String toString() {
		return (sealed ? "(SEALED)" : "") + "QueuedReceivePipeline{" +
				"handling=" + clazz +
				", open=" + !closed +
				", receivePipelineHandlerPolicy=" + receivePipelineHandlerPolicy +
				", core=" + core +
				'}';
	}

	@Override
	public void acquire() throws InterruptedException {
		semaphore.acquire();
	}

	@Override
	public void release() {
		semaphore.release();
	}
}
