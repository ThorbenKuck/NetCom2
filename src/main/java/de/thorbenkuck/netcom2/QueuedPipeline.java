package de.thorbenkuck.netcom2;

import de.thorbenkuck.netcom2.interfaces.Pipeline;
import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class QueuedPipeline<T> implements Pipeline<T> {

	private final Lock lock = new ReentrantLock();
	private Queue<OnReceive<T>> core = new LinkedList<>();
	private boolean closed = false;

	@Override
	public void addLast(OnReceive<T> pipelineService) {
		core.add(pipelineService);
	}

	@Override
	public void addFirst(OnReceive<T> onReceive) {
		Queue<OnReceive<T>> newCore = new LinkedList<>();
		newCore.add(onReceive);
		synchronized (lock) {
			checkClosed();
			newCore.addAll(core);
			core.clear();
			core.addAll(newCore);
		}
	}

	@Override
	public void remove(OnReceive<T> pipelineService) {
		synchronized (lock) {
			checkClosed();
			core.remove(pipelineService);
		}
	}

	@Override
	public void clear() {
		synchronized (lock) {
			checkClosed();
			core.clear();
		}
	}

	@Override
	public void run(User user, Object t) {
		synchronized (lock) {
			checkClosed();
			core.forEach(onReceive -> onReceive.run(user, (T) t));
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
