package de.thorbenkuck.netcom2.network.shared;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public abstract class AbstractPipeline<T, C extends Collection<PipelineElement<T>>> implements Pipeline<T> {

	protected final Lock lock = new ReentrantLock(true);
	private C c;
	private boolean closed;

	protected AbstractPipeline(C c) {
		this.c = c;
	}

	@Override
	public final boolean remove(Consumer<T> pipelineService) {
		return c.remove(new PipelineElement<>(pipelineService));
	}

	@Override
	public final boolean clear() {
		c.clear();
		return c.size() == 0;
	}

	@Override
	public final void run(T t) {
		c.forEach(tPipelineElement -> tPipelineElement.run(t));
	}

	@Override
	public final void close() {
		closed = true;
	}

	@Override
	public final void open() {
		closed = false;
	}

	protected final C getCollection() {
		return this.c;
	}

	protected final void assertClosed() {
		if (closed) {
			throw new RuntimeException("Cannot access closed Pipeline!");
		}
	}

	protected final void lock() {
		lock.lock();
	}

	protected final void unlock() {
		lock.unlock();
	}
}
