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

	private static void checkForWorkerTask() {
		if (countWorkerTasks() == 0) {
			logging.debug("Requiring at least one WorkerTask. Starting one now.");
			startWorkerTask();
		}
	}

	public static void submitTask(Runnable runnable) {
		logging.debug("Adding a new task to the tail of the workQueue");
		logging.trace("Checking for WorkerTasks ..");
		checkForWorkerTask();
		logging.trace("Performing add");
		taskQueue.addLast(runnable);
	}

	public static void submitPriorityTask(Runnable runnable) {
		logging.debug("Adding a new task to the head of the workQueue");
		logging.trace("Checking for WorkerTasks ..");
		checkForWorkerTask();
		logging.trace("Performing add");
		taskQueue.addFirst(runnable);
	}

	public static void startWorkerTask() {
		logging.trace("Creating new WorkerTask");
		WorkerTask workerTask = new WorkerTask(taskQueue, NetComThreadPool::removeWorkerTask);
		try {
			logging.trace("Accessing WorkerThreadPool ..");
			workerThreadPoolLock.lock();
			logging.trace("Submitting new WorkerTask to the WorkerThreadPool");
			workerThreadPool.submit(workerTask);
		} finally {
			workerThreadPoolLock.unlock();
		}
		logging.trace("Storing WorkerTask");
		synchronized (workerTaskList) {
			workerTaskList.add(workerTask);
		}
		logging.debug("There now are " + countWorkerTasks() + " WorkerTasks running.");
	}

	public static void submitCustomWorkerTask(Runnable runnable) {
		logging.debug("Request for custom WorkerTask received");
		try {
			logging.trace("Accessing WorkerThreadPool");
			workerThreadPoolLock.lock();
			logging.trace("Acquired access. Submitting custom WorkerTask");
			workerThreadPool.submit(runnable);
			logging.trace("Custom WorkerTask submitted.");
		} finally {
			workerThreadPoolLock.unlock();
		}
	}

	private static void safeShutdown(ExecutorService executorService, long timeout, TimeUnit timeUnit) {
		logging.debug("Shutting down ExecutorService");
		try {
			logging.trace("Requesting graceful shutdown ..");
			executorService.shutdown();
			logging.trace("Awaiting termination for " + timeout + " " + timeUnit);
			executorService.awaitTermination(timeout, timeUnit);
		} catch (InterruptedException e) {
			if (!executorService.isShutdown()) {
				logging.warn("Interrupted while awaiting termination. Requesting instant shutdown, expect Exceptions");
				logging.catching(e);
				executorService.shutdownNow();
			} else {
				return;
			}
		}
		if (!executorService.isShutdown()) {
			logging.warn("ExecutorService did not shutdown gracefully");
			logging.trace("Requesting instant shutdown");
			executorService.shutdownNow();
		}
	}

	public static void setWorkerThreadPool(ExecutorService executorService) {
		setWorkerThreadPool(executorService, 2, TimeUnit.SECONDS);
	}

	public static void setWorkerThreadPool(ExecutorService executorService, long timeout, TimeUnit timeUnit) {
		try {
			workerThreadPoolLock.lock();
			synchronized (workerTaskList) {
				workerTaskList.forEach(WorkerTask::shutdown);
			}
			safeShutdown(workerThreadPool, timeout, timeUnit);
			workerThreadPool = executorService;
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
			logging.trace("[WorkerTask]: Storing TasksQueue");
			this.taskQueue = taskQueue;
			logging.trace("[WorkerTask]: Storing shutdown hook");
			this.shutdownConsumer = shutdownConsumer;
			logging.instantiated(this);
		}

		private void shutdown() {
			logging.debug("[WorkerTask]: Shutting down");
			logging.trace("[WorkerTask]: Checking for running flag");
			if (running.get()) {
				logging.trace("[WorkerTask]: Updating running flag to shutdown");
				running.set(false);
				logging.trace("[WorkerTask]: Interrupting containing Thread to initiate the shutdown");
				runningThread.interrupt();
			} else {
				logging.debug("[WorkerTask]: Already shut down. Ignoring request");
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
			logging.info("[WorkerTask]: WorkerTask has been initiated");
			logging.trace("[WorkerTask]: Storing containing Thread for shutdown");
			runningThread = Thread.currentThread();
			logging.trace("[WorkerTask]: Entering while loop");
			while (running.get() && !Thread.currentThread().isInterrupted()) {
				try {
					logging.trace("[WorkerTask]: Fetching next Task");
					Runnable runnable = taskQueue.takeFirst();
					logging.trace("[WorkerTask]: Got new Task. Performing Task");
					try {
						runnable.run();
						logging.trace("[WorkerTask]: Task finished successfully");
					} catch (Throwable t) {
						logging.error("[WorkerTask]: Could not complete Task! Encountered unexpected Throwable!", t);
						logging.warn("[WorkerTask]: Trying to continue as if nothing happened.");
					}
				} catch (InterruptedException e) {
					if (running.get()) {
						logging.warn("[WorkerTask]: Interrupted while waiting on Task queue. Shutting down this WorkerTask!");
						logging.catching(e);
						shutdown();
					}
					Thread.currentThread().interrupt();
				}
			}
			logging.debug("[WorkerTask]: Left while loop. Shutdown eminent.");
			logging.trace("[WorkerTask]: Informing shutdown callback");
			shutdownConsumer.accept(this);
			logging.info("[WorkerTask]: Finished");
		}
	}
}
