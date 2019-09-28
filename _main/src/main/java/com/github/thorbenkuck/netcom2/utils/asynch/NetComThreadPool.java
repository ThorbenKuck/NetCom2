package com.github.thorbenkuck.netcom2.utils.asynch;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.UnhandledExceptionContainer;

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
	private static final Value<Integer> maxWorkerProcesses = Value.synchronize(10);
	private static final Value<Integer> readyWorkerTaskCount = Value.synchronize(0);
	private static final Value<Integer> workerTaskCount = Value.synchronize(0);
	private static final Value<Boolean> allowOverflow = Value.synchronize(true);
	private static final Logging logging = Logging.unified();
	private static ExecutorService workerThreadPool = Executors.newCachedThreadPool(new NetComThreadFactory());

	private static void checkForWorkerTask() {
		if (countWorkerTasks() == 0 || readyWorkerTaskCount.get() == 0) {
			logging.debug("Requiring at least one more WorkerTask.");
			logging.trace("Starting one new WorkerProcess now");
			if (maxWorkerProcesses.get() <= countWorkerTasks() && !allowOverflow.get()) {
				logging.debug("Stopping, no overflow is allowed!");
				return;
			}
			startWorkerProcess();
		}
	}

	private synchronized static void decrementReadyWorkerTasks() {
		readyWorkerTaskCount.set(readyWorkerTaskCount.get() - 1);
	}

	private synchronized static void incrementReadyWorkerTasks() {
		readyWorkerTaskCount.set(readyWorkerTaskCount.get() + 1);
	}

	public static void setMaxWorkerProcesses(int to) {
		maxWorkerProcesses.set(to);
	}

	public static void setAllowWorkerProcessOverflow(boolean to) {
		allowOverflow.set(to);
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

	public static void startWorkerProcess(String taskName) {
		if (maxWorkerProcesses.get() <= countWorkerTasks() && !allowOverflow.get()) {
			logging.warn("Policy does not allow for any new WorkerProcess to be started!");
			return;
		}
		logging.trace("Creating new WorkerTask");
		WorkerTask workerTask = new WorkerTask(taskQueue, NetComThreadPool::removeWorkerTask, taskName);
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

	public static void startWorkerProcess() {
		workerTaskCount.set(workerTaskCount.get() + 1);
		startWorkerProcess("WorkerTask" + workerTaskCount.get());
	}

	public static void startWorkerProcesses(int amount) {
		for (int i = 0; i < amount; i++) {
			startWorkerProcess();
		}
	}

	public static void submitCustomProcess(Runnable runnable) {
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
				UnhandledExceptionContainer.catching(e);
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
			decrementReadyWorkerTasks();
		}
	}

	public static int countWorkerTasks() {
		synchronized (workerTaskList) {
			return workerTaskList.size();
		}
	}

	public static int countAvailableWorkerTasks() {
		return readyWorkerTaskCount.get();
	}

	public static String generateDiagnosticOutput() {
		return "WorkerProcess States (available/total): " + countAvailableWorkerTasks() + "/" + countWorkerTasks();
	}

	private static final class WorkerTask implements Runnable {

		private final BlockingDeque<Runnable> taskQueue;
		private final Consumer<WorkerTask> shutdownConsumer;
		private final Value<Boolean> running = Value.synchronize(true);
		private final Logging logging = Logging.unified();
		private final String prefix;
		private Thread runningThread;

		private WorkerTask(BlockingDeque<Runnable> taskQueue, Consumer<WorkerTask> shutdownConsumer, String name) {
			this.prefix = name != null ? "[" + name + "]: " : "";
			logging.trace(prefix + "Storing TasksQueue");
			this.taskQueue = taskQueue;
			logging.trace(prefix + "Storing shutdown hook");
			this.shutdownConsumer = shutdownConsumer;
			logging.instantiated(this);
		}

		private void shutdown() {
			logging.debug(prefix + "Shutting down");
			logging.trace(prefix + "Checking for running flag");
			if (running.get()) {
				logging.trace(prefix + "Updating running flag to shutdown");
				running.set(false);
				logging.trace(prefix + "Interrupting containing Thread to initiate the shutdown");
				runningThread.interrupt();
			} else {
				logging.debug(prefix + "Already shut down. Ignoring request");
			}
		}

		private void postCheck() {
			logging.debug(prefix + "Checking if i can safely shut down");
			if (maxWorkerProcesses.get() < countWorkerTasks()) {
				logging.trace(prefix + "It appears, that i may safely shut down now. Acquiring lock");
				synchronized (NetComThreadPool.class) {
					// Check again, to make sure, that
					// no other working Process has
					// terminated in the meantime
					logging.trace(prefix + "Checking again, if i can safely shut down");
					if (maxWorkerProcesses.get() < countWorkerTasks()) {
						logging.trace(prefix + "I may still shut down. Checking if other WorkerProcesses are in ready state ..");
						// Only shut down, if there
						// are more WorkerProcesses
						// that can handle the next
						// Tasks that come in
						if (countAvailableWorkerTasks() > 1) {
							logging.trace(prefix + "There are more processes, requesting shutdown");
							shutdown();
						}
					}
				}
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
			logging.info(prefix + "WorkerTask has been initiated");
			logging.trace(prefix + "Storing containing Thread for shutdown");
			runningThread = Thread.currentThread();
			logging.trace(prefix + "Entering running loop");
			incrementReadyWorkerTasks();
			while (running.get() && !Thread.currentThread().isInterrupted()) {
				try {
					logging.trace(prefix + "Awaiting next Task");
					Runnable runnable = taskQueue.takeFirst();
					logging.trace(prefix + "Fetched next Task to execute");
					decrementReadyWorkerTasks();
					logging.trace(prefix + "Got new Task. Performing Task");
					logging.trace(prefix + "Task: " + runnable.toString());
					try {
						logging.debug(prefix + "Starting requested Task");
						runnable.run();
						logging.debug(prefix + "Finished requested Task");
						logging.trace(prefix + "Task finished successfully");
					} catch (Throwable t) {
						logging.error(prefix + "Could not complete Task! Encountered unexpected Throwable!", t);
						UnhandledExceptionContainer.catching(t);
						logging.warn(prefix + "Trying to continue as if nothing happened.");
					}
				} catch (InterruptedException e) {
					if (running.get()) {
						logging.warn(prefix + "Interrupted while waiting on Task queue. Shutting down this WorkerTask!", e);
						UnhandledExceptionContainer.catching(e);
						shutdown();
					}
					Thread.currentThread().interrupt();
				}
				incrementReadyWorkerTasks();
				postCheck();
			}
			logging.debug(prefix + "Left while loop. Shutdown eminent.");
			logging.trace(prefix + "Informing shutdown callback");
			shutdownConsumer.accept(this);
			logging.info(prefix + "Finished");
		}
	}
}
