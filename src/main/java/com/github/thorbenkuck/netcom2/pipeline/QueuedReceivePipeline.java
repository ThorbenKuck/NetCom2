package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.keller.annotations.Synchronized;
import com.github.thorbenkuck.keller.annotations.Tested;
import com.github.thorbenkuck.netcom2.exceptions.PipelineAccessException;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * A queued ReceivePipeline implementation.
 *
 * @param <T> The type
 * @version 1.0
 * @since 1.0
 */
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.pipeline.EmptyReceivePipelineConditionTest")
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.pipeline.QueuedReceivePipelineTest")

public class QueuedReceivePipeline<T> implements ReceivePipeline<T> {

	private final Queue<PipelineReceiver<T>> core = new LinkedList<>();
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
	private final Logging logging = Logging.unified();
	private final Lock policyLock = new ReentrantLock();
	private final Class<T> clazz;
	private final ReceiveObjectHandlerWrapper receiveObjectHandlerWrapper = new ReceiveObjectHandlerWrapper();
	private boolean closed = false;
	private boolean sealed = false;
	private ReceivePipelineHandlerPolicy receivePipelineHandlerPolicy = ReceivePipelineHandlerPolicy.ALLOW_SINGLE;

	/**
	 * Create a queued ReceivePipeline for the specified class
	 *
	 * @param clazz The class
	 */
	public QueuedReceivePipeline(final Class<T> clazz) {
		NetCom2Utils.parameterNotNull(clazz);
		this.clazz = clazz;
		logging.instantiated(this);
	}

	/**
	 * Tries to put the specified connection, session and S into the specified pipeline receiver.
	 *
	 * @param receiver   The PipelineReceiver
	 * @param connectionContext The connection
	 * @param session    The session
	 * @param s          The S
	 * @param <S>        The type
	 */
	private <S> void run(final PipelineReceiver<S> receiver, final ConnectionContext connectionContext, final Session session, final S s) {
		OnReceiveTriple<S> onReceiveTriple = receiver.getOnReceive();
		if (onReceiveTriple == null) {
			logging.error("Found null OnReceive in PipelineReceiver " + receiver);
			throw new IllegalStateException("Found null registration for " + s.getClass());
		}

		onReceiveTriple.execute(connectionContext, session, s);
	}

	/**
	 * Add a fail to the specified CanBeRegistered
	 *
	 * @param canBeRegistered The CanBeRegistered
	 */
	private void falseAdd(final CanBeRegistered canBeRegistered) {
		canBeRegistered.onAddFailed();
	}

	/**
	 * Execute the specified runnable (on the current thread) if the pipeline is open.
	 *
	 * @param runnable The runnable to execute
	 */
	private void ifOpen(final Runnable runnable) {
		if (!closed) {
			runnable.run();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceivePipelineCondition<T> addLast(final OnReceive<T> onReceive) {
		NetCom2Utils.parameterNotNull(onReceive);
		return addLast(new OnReceiveWrapper<>(onReceive));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceivePipelineCondition<T> addLast(final OnReceiveSingle<T> onReceiveSingle) {
		NetCom2Utils.parameterNotNull(onReceiveSingle);
		return addLast(new OnReceiveSingleWrapper<>(onReceiveSingle));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceivePipelineCondition<T> addLast(final OnReceiveTriple<T> pipelineService) {
		NetCom2Utils.parameterNotNull(pipelineService);
		final PipelineReceiver<T> pipelineReceiver = new PipelineReceiver<>(pipelineService);
		ifClosed(() -> falseAdd(pipelineService));
		ifOpen(() -> {
			synchronized (core) {
				core.add(pipelineReceiver);
			}
			pipelineService.onRegistration();
			logging.debug("Registering onReceive: " + pipelineReceiver);
		});
		return new ReceivePipelineConditionImpl<>(pipelineReceiver);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceivePipelineCondition<T> addFirst(final OnReceive<T> onReceive) {
		NetCom2Utils.parameterNotNull(onReceive);
		return addFirst(new OnReceiveWrapper<>(onReceive));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceivePipelineCondition<T> addFirst(final OnReceiveSingle<T> pipelineService) {
		NetCom2Utils.parameterNotNull(pipelineService);
		return addFirst(new OnReceiveSingleWrapper<>(pipelineService));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceivePipelineCondition<T> addFirst(final OnReceiveTriple<T> pipelineService) {
		NetCom2Utils.parameterNotNull(pipelineService);
		final PipelineReceiver<T> pipelineReceiver = new PipelineReceiver<>(pipelineService);

		if (isClosed()) {
			falseAdd(pipelineService);
		} else {
			final Queue<PipelineReceiver<T>> newCore = new LinkedList<>();
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceivePipelineCondition<T> addFirstIfNotContained(final OnReceive<T> pipelineService) {
		NetCom2Utils.parameterNotNull(pipelineService);
		return addFirstIfNotContained(new OnReceiveWrapper<>(pipelineService));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceivePipelineCondition<T> addFirstIfNotContained(final OnReceiveSingle<T> pipelineService) {
		NetCom2Utils.parameterNotNull(pipelineService);
		return addFirstIfNotContained(new OnReceiveSingleWrapper<>(pipelineService));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceivePipelineCondition<T> addFirstIfNotContained(final OnReceiveTriple<T> pipelineService) {
		NetCom2Utils.parameterNotNull(pipelineService);
		if (!contains(pipelineService)) {
			return addFirst(pipelineService);
		}
		return ReceivePipelineCondition.empty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceivePipelineCondition<T> addLastIfNotContained(final OnReceive<T> pipelineService) {
		NetCom2Utils.parameterNotNull(pipelineService);
		return addLastIfNotContained(new OnReceiveWrapper<>(pipelineService));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceivePipelineCondition<T> addLastIfNotContained(final OnReceiveSingle<T> pipelineService) {
		NetCom2Utils.parameterNotNull(pipelineService);
		return addLastIfNotContained(new OnReceiveSingleWrapper<>(pipelineService));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceivePipelineCondition<T> addLastIfNotContained(final OnReceiveTriple<T> pipelineService) {
		NetCom2Utils.parameterNotNull(pipelineService);
		if (!contains(pipelineService)) {
			return addLast(pipelineService);
		}
		return ReceivePipelineCondition.empty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceivePipelineCondition<T> to(final Object object) {
		NetCom2Utils.parameterNotNull(object);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(final OnReceiveTriple<T> onReceiveTriple) {
		NetCom2Utils.parameterNotNull(onReceiveTriple);
		synchronized (core) {
			return core.contains(new PipelineReceiver<>(onReceiveTriple));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(final OnReceive<T> onReceive) {
		NetCom2Utils.parameterNotNull(onReceive);
		return contains(new OnReceiveWrapper<>(onReceive));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(final OnReceiveSingle<T> onReceiveSingle) {
		NetCom2Utils.parameterNotNull(onReceiveSingle);
		return contains(new OnReceiveSingleWrapper<>(onReceiveSingle));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSealed() {
		return sealed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isClosed() {
		return closed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return core.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ifClosed(final Consumer<ReceivePipeline<T>> consumer) {
		NetCom2Utils.parameterNotNull(consumer);
		ifClosed(() -> consumer.accept(this));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ifClosed(final Runnable runnable) {
		NetCom2Utils.parameterNotNull(runnable);
		if (closed) {
			runnable.run();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setReceivePipelineHandlerPolicy(final ReceivePipelineHandlerPolicy receivePipelineHandlerPolicy) {
		NetCom2Utils.parameterNotNull(receivePipelineHandlerPolicy);
		try {
			policyLock.lock();
			this.receivePipelineHandlerPolicy = receivePipelineHandlerPolicy;
		} finally {
			policyLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(final OnReceive<T> pipelineService) {
		NetCom2Utils.parameterNotNull(pipelineService);
		synchronized (core) {
			core.remove(new PipelineReceiver<>(new OnReceiveWrapper<>(pipelineService)));
			pipelineService.onUnRegistration();
		}
	}

	@Override
	public void remove(OnReceiveSingle<T> pipelineService) {
		NetCom2Utils.parameterNotNull(pipelineService);
		synchronized (core) {
			core.remove(new PipelineReceiver<>(new OnReceiveSingleWrapper<>(pipelineService)));
			pipelineService.onUnRegistration();
		}
	}

	@Override
	public void remove(OnReceiveTriple<T> pipelineService) {
		NetCom2Utils.parameterNotNull(pipelineService);
		synchronized (core) {
			core.remove(new PipelineReceiver<>(pipelineService));
			pipelineService.onUnRegistration();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		if (isClosed()) {
			throw new PipelineAccessException("Cannot clear an closed Pipeline!");
		}
		synchronized (core) {
			core.clear();
		}
	}

	/**
	 * Runs a certain T through this ReceivePipeline.
	 * <p>
	 * It will check every {@link ReceivePipelineCondition}, to see whether or not the so registered OnReceive will be executed
	 *
	 * @param connectionContext the {@link Connection}, which is associated with the receiving of the T
	 * @param session    the {@link Session}, which is associated with the receiving of the T
	 * @param t          the Object, which should be run through this ReceivePipeline
	 */
	@Override
	public void run(ConnectionContext connectionContext, Session session, T t) {
		NetCom2Utils.parameterNotNull(connectionContext, session, t);
		try {
			synchronized (core) {
				core.stream()
						.filter(pipelineReceiver -> pipelineReceiver.test(connectionContext, session, t))
						.forEachOrdered(pipelineReceiver -> run(pipelineReceiver, connectionContext, session, t));
			}
		} catch (final Exception e) {
			logging.error("Encountered exception!", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		requiredNotSealed();
		logging.debug("Closing ReceivePipeline for " + clazz);
		closed = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void seal() {
		logging.debug("Sealing ReceivePipeline for " + clazz);
		sealed = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void open() {
		requiredNotSealed();
		logging.debug("Opening ReceivePipeline for " + clazz);
		closed = false;
	}

	/**
	 * Returns the number of handlers contained within the ReceivePipeline.
	 *
	 * @return the count of all ReceivePipelineHandler
	 */
	@Override
	public int size() {
		synchronized (core) {
			return core.size();
		}
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof QueuedReceivePipeline)) return false;

		final QueuedReceivePipeline<?> that = (QueuedReceivePipeline<?>) o;

		return closed == that.closed && sealed == that.sealed && core.equals(that.core)
				&& logging.equals(that.logging) && policyLock.equals(that.policyLock)
				&& clazz.equals(that.clazz) && receiveObjectHandlerWrapper.equals(that.receiveObjectHandlerWrapper)
				&& receivePipelineHandlerPolicy == that.receivePipelineHandlerPolicy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return (sealed ? "(SEALED)" : "") + "QueuedReceivePipeline{" +
				"handling=" + clazz +
				", open=" + !closed +
				", receivePipelineHandlerPolicy=" + receivePipelineHandlerPolicy +
				", core=" + core +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void acquire() throws InterruptedException {
		readWriteLock.readLock().lock();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		readWriteLock.readLock().unlock();
	}

	/**
	 * Throws PipelineAccessException if pipeline is closed.
	 *
	 * @throws PipelineAccessException if the pipeline is closed
	 */
	protected void requiresOpen() {
		if (closed) {
			throw new PipelineAccessException("ReceivePipeline Closed!");
		}
	}

	/**
	 * Throws PipelineAccessException if pipeline is sealed.
	 *
	 * @throws PipelineAccessException if the pipeline is sealed
	 */
	protected void requiredNotSealed() {
		if (sealed) {
			throw new PipelineAccessException("ReceivePipeline is sealed!");
		}
	}
}
