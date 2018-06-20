package com.github.thorbenkuck.netcom2.utility;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;

import java.util.function.Consumer;

/**
 * NetCom2 uses its own Threads such that developers can easily distinguish between NetCom2 Threads
 * and application Threads.
 *
 * @version 1.0
 * @since 1.0
 */
public class NetComThread extends Thread {


	private final Value<Boolean> started = Value.synchronize(false);
	private Consumer<NetComThread> finishedCallback;
	private int count = -1;

	/**
	 * {@inheritDoc}
	 */
	public NetComThread() {
		setup();
		count = -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public NetComThread(Runnable runnable) {
		super(runnable);
		setup();
		count = -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public NetComThread(Runnable runnable, int count) {
		super(runnable);
		setup();
		this.count = count;
	}

	/**
	 * Makes this Thread
	 * <ul>
	 * <li><b>not</b> daemon</li>
	 * <li>priority 7</li>
	 * <li>named like a NetCom2 Thread</li>
	 * <li>log uncaught exceptions</li>
	 * </ul>
	 * The priority is such that it is higher than normal, but still not highest
	 */
	private void setup() {
		setDaemon(false);
		setPriority(7);
		setName(NetComThreadFactory.NET_COM_THREAD_NAME);
		setUncaughtExceptionHandler((encounteredThread, throwable) -> Logging.unified().error("Unhandled Exception on NetComThread!", throwable));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		started.set(true);
		super.run();
		finished();
	}


	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		if (count >= 0) {
			return getName() + "[priority=" + getPriority() + ", number=" + count + "]";
		} else {
			return getName() + "[priority=" + getPriority() + "]";
		}
	}

	/**
	 * Removed this thread from its thread container, if it isn't null.
	 */
	public void finished() {
		synchronized (this) {
			if (finishedCallback != null) {
				finishedCallback.accept(this);
			}
		}
	}

	public void setFinishedCallback(Consumer<NetComThread> callback) {
		if (started.get()) {
			return;
		}
		synchronized (this) {
			this.finishedCallback = callback;
		}
	}

	public int getNumber() {
		return count;
	}

	public void setNumber(int number) {
		if (number < 0) {
			throw new IllegalArgumentException("The formal Number of this Thread must be greater than -1!");
		}
		this.count = number;
	}
}
