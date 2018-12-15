package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

final class NativeBlockingEventLoop implements EventLoop {

	private final List<Connection> core = new ArrayList<>();
	private final ConnectionShutdownHook SHUTDOWN_HOOK = new ConnectionShutdownHook();
	private final Logging logging = Logging.unified();
	private final Value<Boolean> started = Value.synchronize(false);
	private final BlockingQueue<RawDataPackage> dataQueue = new LinkedBlockingQueue<>();
	private final RawDataPackageProcess process = new RawDataPackageProcess();
	private final List<RawDataPackageProcess> separateProcesses = new ArrayList<>();

	NativeBlockingEventLoop() {
		logging.instantiated(this);
	}

	private void prepareRawData(final Queue<RawData> rawDataQueue, final Connection connection) {
		final RawDataPackage rawDataPackage = new RawDataPackage(rawDataQueue, connection);
		dataQueue.add(rawDataPackage);
		if (dataQueue.size() > 10) {
			final RawDataPackageProcess rawDataPackageProcess = new RawDataPackageProcess();
			synchronized (separateProcesses) {
				separateProcesses.add(rawDataPackageProcess);
			}
			NetComThreadPool.submitCustomProcess(rawDataPackageProcess);
		}
	}

	private void decreaseCustomWorkerProcesses() {
		synchronized (separateProcesses) {
			try {
				final RawDataPackageProcess rawDataPackageProcess = separateProcesses.remove(0);
				rawDataPackageProcess.stop();
			} catch (final IndexOutOfBoundsException ignored) {
				// There are now Processes in the Queue. Ignore this request
			}
		}
	}

	@Override
	public final void register(final Connection connection) throws IOException {
		if (!started.get()) {
			throw new IOException("EventLoop has not been started!");
		}
		synchronized (core) {
			core.add(connection);
			connection.addShutdownHook(SHUTDOWN_HOOK);
			connection.read(queue -> prepareRawData(queue, connection));
		}
	}

	@Override
	public final void unregister(final Connection connection) {
		synchronized (core) {
			core.remove(connection);
			connection.removeShutdownHook(SHUTDOWN_HOOK);
		}
	}

	@Override
	public final synchronized void start() {
		if (started.get()) {
			logging.warn("Tried to start a already started EventLoop!");
			return;
		}
		logging.debug("Starting EventLoop..");
		started.set(true);
		NetComThreadPool.submitCustomProcess(process);
		logging.debug("EventLoop started");
	}

	@Override
	public final synchronized void shutdown() {
		logging.debug("Shutting EventLoop down..");
		started.set(false);
		process.stop();
	}

	@Override
	public final synchronized void shutdownNow() {
		shutdown();
		synchronized (core) {
			for (final Connection connection : core) {
				try {
					connection.close();
				} catch (final IOException e) {
					logging.catching(e);
				}
			}
			core.clear();
		}
	}

	@Override
	public final boolean isRunning() {
		return started.get();
	}

	@Override
	public final int workload() {
		synchronized (core) {
			return core.size();
		}
	}

	private final class ConnectionShutdownHook implements Consumer<Connection> {

		@Override
		public final void accept(final Connection connection) {
			unregister(connection);
		}
	}

	private final class RawDataPackageProcess implements Runnable {

		private final Value<Boolean> running = Value.synchronize(false);
		private Thread containingThread;

		@Override
		public synchronized void run() {
			containingThread = Thread.currentThread();
			running.set(true);
			while (running.get()) {
				try {
					if (dataQueue.size() < 3) {
						decreaseCustomWorkerProcesses();
					}
					// This Check is done,
					// to catch the case,
					// that we are terminated
					// by the process decrease
					if (!running.get()) {
						continue;
					}
					synchronized (this) {
						final RawDataPackage rawDataPackage = dataQueue.take();
						final Queue<RawData> rawData = rawDataPackage.getRawData();

						while (rawData.peek() != null) {
							rawDataPackage.getConnection()
									.context()
									.receive(rawData.poll());
						}
					}
				} catch (final InterruptedException e) {
					if (running.get()) {
						logging.error("RawDataPackageProcess has been interrupted. Stopping any further reads ..", e);
						running.set(false);
						if (isRunning()) {
							shutdown();
						}
					}
				}
			}
			// in every case here, running
			// will be set to false. Therefor
			// we do not have to change it back
		}

		public void stop() {
			running.set(false);
			if (containingThread != null) {
				containingThread.interrupt();
			}
		}
	}
}
