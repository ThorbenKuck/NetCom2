package com.github.thorbenkuck.netcom2.utility.threaded;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.utility.NetComThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class NetComThreadPool {

	private static final Lock workerThreadPoolLock = new ReentrantLock(true);
	private static final BlockingDeque<Runnable> taskQueue = new LinkedBlockingDeque<>();
	private static final List<WorkerTask> workerTaskList = new ArrayList<>();
	private static final Logging logging = Logging.unified();
	private static ExecutorService workerThreadPool = Executors.newCachedThreadPool(new NetComThreadFactory());

	public static void submitTask(Runnable runnable) {
		taskQueue.addLast(runnable);
	}

	public static void submitPriorityTask(Runnable runnable) {
		taskQueue.addFirst(runnable);
	}

	public static void startWorkerTask() {
		try {
			WorkerTask workerTask = new WorkerTask(taskQueue, NetComThreadPool::removeWorkerTask);
			logging.trace("Accessing WorkerThreadPool ..");
			workerThreadPoolLock.lock();
			logging.trace("");
			workerThreadPool.submit(workerTask);
		} finally {
			workerThreadPoolLock.unlock();
		}
	}

	public static void submitCustomWorkerThread(Runnable runnable) {
		try {
			workerThreadPoolLock.lock();
			workerThreadPool.submit(runnable);
		} finally {
			workerThreadPoolLock.unlock();
		}
	}

	private static void safeShutdown(ExecutorService executorService, long timeout, TimeUnit timeUnit) {
		try {
			executorService.awaitTermination(timeout, timeUnit);
		} catch (InterruptedException e) {
			if (!executorService.isShutdown()) {
				logging.warn("Interrupted while awaiting termination. Requesting instant shutdown, expect interrupted Exceptions");
				logging.catching(e);
				executorService.shutdownNow();
			}
		}
	}

	public static void setWorkerThreadPool(ExecutorService executorService) {
		setWorkerThreadPool(executorService, 2, TimeUnit.SECONDS);
	}

	public static void setWorkerThreadPool(ExecutorService executorService, long timeout, TimeUnit timeUnit) {
		try {
			workerThreadPoolLock.lock();
			synchronized (workerTaskList) {
				workerTaskList.forEach(WorkerTask::shutDown);
			}
			safeShutdown(workerThreadPool, timeout, timeUnit);
			workerThreadPool = executorService;
			workerThreadPool.shutdown();
		} finally {
			workerThreadPoolLock.unlock();
		}
	}

	private static void removeWorkerTask(WorkerTask workerTask) {
		synchronized (workerTaskList) {
			workerTaskList.remove(workerTask);
		}
	}

	public static int countWorkerTasks() {
		synchronized (workerTaskList) {
			return workerTaskList.size();
		}
	}

	private static final class WorkerTask implements Runnable {

		private final BlockingDeque<Runnable> taskQueue;
		private final Consumer<WorkerTask> shutdownConsumer;
		private final Value<Boolean> running = Value.synchronize(true);
		private final Logging logging = Logging.unified();
		private Thread runningThread;

		private WorkerTask(BlockingDeque<Runnable> taskQueue, Consumer<WorkerTask> shutdownConsumer) {
			this.taskQueue = taskQueue;
			this.shutdownConsumer = shutdownConsumer;
			logging.instantiated("WorkerTask");
		}

		private void shutDown() {
			if (!running.get()) {
				running.set(false);
				runningThread.interrupt();
			}
		}

		/**
		 * When an object implementing interface <code>Runnable</code> is used
		 * to create a thread, starting the thread causes the object's
		 * <code>run</code> method to be called in that separately executing
		 * thread.
		 * <p>
		 * The general contract of the method <code>run</code> is that it may
		 * take any action whatsoever.
		 *
		 * @see Thread#run()
		 */
		@Override
		public void run() {
			runningThread = Thread.currentThread();
			while (running.get() && !Thread.currentThread().isInterrupted()) {
				try {
					Runnable runnable = taskQueue.takeFirst();
					runnable.run();
				} catch (InterruptedException e) {
					if (running.get()) {
						logging.warn("Interrupted while waiting on Task queue. Shutting down this WorkerTask!");
						logging.catching(e);
						shutDown();
					}
					Thread.currentThread().interrupt();
				}
			}

			shutdownConsumer.accept(this);
		}
	}
}
