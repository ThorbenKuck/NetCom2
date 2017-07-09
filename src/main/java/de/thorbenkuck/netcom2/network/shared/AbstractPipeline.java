package de.thorbenkuck.netcom2.network.shared;

import de.thorbenkuck.netcom2.annotations.Synchronized;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Synchronized
public abstract class AbstractPipeline<T, C extends Collection<PipelineElement<T>>> implements Pipeline<T> {

	protected final Lock lock = new ReentrantLock(true);
	private final Lock accessLock = new ReentrantLock();
	private final C collection;
	private boolean closed = false;
	private boolean sealed = false;

	protected AbstractPipeline(C collection) {
		this.collection = collection;
	}

	@Override
	public PipelineCondition<T> addLast(Consumer<T> consumer) {
		PipelineElement<T> pipelineElement = new PipelineElement<>(consumer);
		try {
			lock();
			collection.add(pipelineElement);
		} finally {
			unlock();
		}
		return new PipelineConditionImpl<>(pipelineElement);
	}

	@Override
	public PipelineCondition<T> addFirst(Consumer<T> consumer) {
		Queue<PipelineElement<T>> temp = new LinkedList<>(getCollection());
		PipelineElement<T> pipelineElement = new PipelineElement<>(consumer);
		try {
			lock();
			collection.clear();
			collection.add(pipelineElement);
			collection.addAll(temp);
		} finally {
			unlock();
		}
		return new PipelineConditionImpl<>(pipelineElement);
	}

	@Override
	public boolean remove(Consumer<T> pipelineService) {
		return collection.remove(new PipelineElement<>(pipelineService));
	}

	@Override
	public boolean clear() {
		try {
			lock();
			collection.clear();
			return collection.size() == 0;
		} finally {
			unlock();
		}
	}

	@Override
	public void run(T t) {
		try {
			lock();
			collection.forEach(tPipelineElement -> tPipelineElement.run(t));
		} finally {
			unlock();
		}
	}

	@Override
	public final void close() {
		try {
			accessLock.lock();
			if (! sealed) {
				closed = true;
			}
		} finally {
			accessLock.unlock();
		}
	}

	@Override
	public final void open() {
		try {
			accessLock.lock();
		if (! sealed) {
			closed = false;
		}
		} finally {
			accessLock.unlock();
		}
	}

	@Override
	public final void seal() {
		try {
			accessLock.lock();
		sealed = true;
		} finally {
			accessLock.unlock();
		}
	}

	@Override
	public final boolean isSealed() {
		return sealed;
	}

	@Override
	public final boolean isOpen() {
		return !closed;
	}

	@Override
	public final void ifClosed(Consumer<Pipeline<T>> consumer) {
		ifClosed(() -> consumer.accept(this));
	}

	@Override
	public final void ifClosed(Runnable runnable) {
		if (closed) {
			runnable.run();
		}
	}

	protected final void lock() {
		lock.lock();
	}

	protected final C getCollection() {
		return this.collection;
	}

	protected final void unlock() {
		lock.unlock();
	}

	protected final void assertClosed() {
		if (closed) {
			throw new RuntimeException("Cannot getDefaultJavaSerialization closed Pipeline!");
		}
	}
}
