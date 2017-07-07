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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class QueuedReceivePipeline<T> implements ReceivePipeline<T> {

	private final Queue<PipelineReceiverImpl<T>> core = new LinkedList<>();
	private final Logging logging = Logging.unified();
	private final Lock policyLock = new ReentrantLock();
	private final Class<T> clazz;
	private final ReceiveObjectHandlerWrapper receiveObjectHandlerWrapper = new ReceiveObjectHandlerWrapper();
	private boolean closed = false;
	private boolean sealed = false;
	private ReceivePipelineHandlerPolicy receivePipelineHandlerPolicy = ReceivePipelineHandlerPolicy.ALLOW_SINGLE;

	public QueuedReceivePipeline(Class<T> clazz) {
		this.clazz = clazz;
	}

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

		if (isClosed()) {
			falseAdd(pipelineService);
		} else {
			Queue<PipelineReceiverImpl<T>> newCore = new LinkedList<>();
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
	public ReceivePipelineCondition<T> addFirstIfNotContained(OnReceive<T> pipelineService) {
		return addFirstIfNotContained(new OnReceiveWrapper<>(pipelineService));
	}

	@Override
	public ReceivePipelineCondition<T> addFirstIfNotContained(OnReceiveSingle<T> pipelineService) {
		return addFirstIfNotContained(new OnReceiveSingleWrapper<>(pipelineService));
	}

	public boolean contains(OnReceiveTriple<T> onReceiveTriple) {
		return core.contains(new PipelineReceiverImpl<>(onReceiveTriple));
	}

	public boolean contains(OnReceive<T> onReceive) {
		return contains(new OnReceiveWrapper<>(onReceive));
	}

	public boolean contains(OnReceiveSingle<T> onReceiveSingle) {
		return contains(new OnReceiveSingleWrapper<>(onReceiveSingle));
	}

	@Override
	public ReceivePipelineCondition<T> addFirstIfNotContained(OnReceiveTriple<T> pipelineService) {
		if(!contains(pipelineService)) {
			return addFirst(pipelineService);
		}
		return ReceivePipelineCondition.empty();
	}

	@Override
	public ReceivePipelineCondition<T> addLastIfNotContained(OnReceive<T> pipelineService) {
		return addLastIfNotContained(new OnReceiveWrapper<>(pipelineService));
	}

	@Override
	public ReceivePipelineCondition<T> addLastIfNotContained(OnReceiveSingle<T> pipelineService) {
		return addLastIfNotContained(new OnReceiveSingleWrapper<>(pipelineService));
	}

	@Override
	public ReceivePipelineCondition<T> addLastIfNotContained(OnReceiveTriple<T> pipelineService) {
		if(!contains(pipelineService)) {
			return addLast(pipelineService);
		}
		return ReceivePipelineCondition.empty();
	}

	@Override
	public ReceivePipelineCondition<T> to(Object object) {
		requiresOpen();
		requiredNotSealed();
		try {
			policyLock.lock();
			receivePipelineHandlerPolicy.prepare(this);
			ReceivePipelineCondition<T> toReturn = addFirst(receiveObjectHandlerWrapper.wrap(object, clazz));
			receivePipelineHandlerPolicy.afterAdding(this);
			return toReturn;
		} finally {
			policyLock.unlock();
		}
	}

	@Override
	public void setReceivePipelineHandlerPolicy(ReceivePipelineHandlerPolicy receivePipelineHandlerPolicy) {
		try {
			policyLock.lock();
			this.receivePipelineHandlerPolicy = receivePipelineHandlerPolicy;
		} finally {
			policyLock.unlock();
		}
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
	public void run(Connection connection, Session session, T t) {
		try {
			synchronized (core) {
				core.stream()
						.filter(pipelineReceiver -> pipelineReceiver.test(connection, session, t))
						.forEachOrdered(pipelineReceiver -> pipelineReceiver.getOnReceive().accept(connection, session, t));
			}
		} catch (Exception e) {
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
	public boolean isSealed() {
		return sealed;
	}

	@Override
	public void open() {
		requiredNotSealed();
		logging.debug("Opening ReceivePipeline for " + clazz);
		closed = false;
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

	@Override
	public boolean isEmpty() {
		return core.isEmpty();
	}

	protected void requiredNotSealed() {
		if (sealed) {
			throw new PipelineAccessException("ReceivePipeline is sealed!");
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (! (o instanceof QueuedReceivePipeline)) return false;

		QueuedReceivePipeline<?> that = (QueuedReceivePipeline<?>) o;

		if (closed != that.closed) return false;
		if (sealed != that.sealed) return false;
		if (! core.equals(that.core)) return false;
		if (! logging.equals(that.logging)) return false;
		if (! policyLock.equals(that.policyLock)) return false;
		if (! clazz.equals(that.clazz)) return false;
		if (! receiveObjectHandlerWrapper.equals(that.receiveObjectHandlerWrapper))
			return false;
		return receivePipelineHandlerPolicy == that.receivePipelineHandlerPolicy;
	}

	@Override
	public String toString() {
		return "QueuedReceivePipeline{" +
				"handling=" + clazz +
				", core=" + core +
				", closed=" + closed +
				", sealed=" + sealed +
				", receivePipelineHandlerPolicy=" + receivePipelineHandlerPolicy +
				'}';
	}

	protected void requiresOpen() {
		if (closed) {
			throw new PipelineAccessException("ReceivePipeline Closed!");
		}
	}
}
