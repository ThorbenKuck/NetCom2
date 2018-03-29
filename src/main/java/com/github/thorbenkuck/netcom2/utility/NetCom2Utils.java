package com.github.thorbenkuck.netcom2.utility;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.pipeline.Wrapper;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This Utility Class defines some methods, that are commonly used within NetCom2
 * <p>
 * Since those checks are stateless, they are static.
 * Thread-Safety should not be necessary, else whatever needs Thread-Safety does not belong here
 * <p>
 * Since this class is used mainly internally, it should not be used outside of the internal project.
 * If you want to use it anyways, use it with care! This class is highly likely to be subject of change!
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
public class NetCom2Utils {

	/*
	 * The following is used internally in this class only
	 */
	@APILevel
	private static final Logging logging = Logging.unified();
	@APILevel
	private static final Wrapper wrapper = new Wrapper();
	@APILevel
	private static final Semaphore synchronization = new Semaphore(1);
	@APILevel
	private static final AtomicBoolean netComThreadRunning = new AtomicBoolean(false);
	/*
	 * The following is needed, for the asynchronous API
	 * it defines multiple Thread-necessities, as well as
	 * an custom ThreadFactory.
	 */
	private static final BlockingQueue<Runnable> runnableQueue = new LinkedBlockingQueue<>();
	private static final ThreadFactory THREAD_FACTORY = createNewDaemonThreadFactory();
	private static final ThreadFactory NON_DAEMON_THREAD_FACTORY = createNewNonDaemonThreadFactory();
	private static final ExecutorService queueExecutorService = Executors.newSingleThreadExecutor(THREAD_FACTORY);
	private static final ExecutorService NET_COM_THREAD = createNewCachedExecutorService();

	static {
		netComThreadRunning.set(true);
		queueExecutorService.execute(() -> {
			do {
				try {
					Runnable runnable = runnableQueue.take();
					assertNotNull(runnable);
					runnable.run();
				} catch (InterruptedException e) {
					logging.catching(e);
				}
			} while (netComThreadRunning.get());
		});
	}

	/**
	 * This checks for null. If the provided Object <code>o</code> is found to be null, a {@link NullPointerException}
	 * will be thrown.
	 *
	 * @param o the Object, that should be tested against null
	 * @see #assertNotNull(Object...)
	 */
	@APILevel
	public static void assertNotNull(final Object o) {
		if (o == null) {
			throw new NullPointerException();
		}
	}

	/**
	 * Like {@link #assertNotNull(Object)}, this method checks for null.
	 * <p>
	 * The difference is, that you may add any Number of Objects to check here.
	 *
	 * @param objects an array of Objects, that should be checked against null
	 * @see #assertNotNull(Object)
	 */
	@APILevel
	public static void assertNotNull(final Object... objects) {
		for (final Object object : objects) {
			assertNotNull(object);
		}
	}

	/**
	 * This Method checks, if the given Object is null.
	 * <p>
	 * Other than {@link #assertNotNull(Object)} and  {@link #assertNotNull(Object...)}, this method will throw an
	 * IllegalArgumentException.
	 * <p>
	 * So basically this Method checks, for illegal arguments, which in fact are null.
	 *
	 * @param object the Object that should be checked against null
	 * @see #parameterNotNull(Object...)
	 */
	@APILevel
	public static void parameterNotNull(final Object object) {
		if (object == null) {
			throw new IllegalArgumentException("Null is not a valid parameter!");
		}
	}

	/**
	 * This Method checks, if the given Objects are null.
	 * <p>
	 * Other than {@link #assertNotNull(Object)} and  {@link #assertNotNull(Object...)}, this method will throw an
	 * IllegalArgumentException.
	 * <p>
	 * So basically this Method checks, for illegal arguments, which in fact are null.
	 *
	 * @param objects an array of Objects that should be checked against null
	 * @see #parameterNotNull(Object)
	 */
	@APILevel
	public static void parameterNotNull(final Object... objects) {
		if (objects == null)
			throw new IllegalArgumentException("Null is not a valid parameter!");
		for (final Object object : objects) {
			parameterNotNull(object);
		}
	}

	/**
	 * This Method is a short form for {@link Wrapper#wrap(OnReceiveSingle)} and uses an static instance of <code>Wrapper</code>
	 * <p>
	 * This means you do not need to access <code>Wrapper</code> instances, whenever an <code>OnReceive</code> should be wrapped
	 *
	 * @param onReceiveSingle the {@link OnReceiveSingle} that should be wrapped
	 * @param <T>             the generic Type of the OnReceiveSingle
	 * @return a wrapped {@link OnReceiveTriple}, that is used internally of the {@link com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline}
	 * @see com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline
	 * @see #wrap(OnReceive)
	 */
	@APILevel
	public static <T> OnReceiveTriple<T> wrap(OnReceiveSingle<T> onReceiveSingle) {
		parameterNotNull(onReceiveSingle);
		return wrapper.wrap(onReceiveSingle);
	}

	/**
	 * This Method is a short form for {@link Wrapper#wrap(OnReceive)} and uses an static instance of <code>Wrapper</code>
	 * <p>
	 * This means you do not need to access <code>Wrapper</code> instances, whenever an <code>OnReceive</code> should be wrapped
	 *
	 * @param onReceive the {@link OnReceive} that should be wrapped
	 * @param <T>       the generic Type of the OnReceive
	 * @return a wrapped {@link OnReceiveTriple}, that is used internally of the {@link com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline}
	 * @see com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline
	 * @see #wrap(OnReceiveSingle)
	 */
	@APILevel
	public static <T> OnReceiveTriple<T> wrap(OnReceive<T> onReceive) {
		parameterNotNull(onReceive);
		return wrapper.wrap(onReceive);
	}

	/**
	 * Executes an runnable, but synchronized synchronizes over an Semaphore.
	 * <p>
	 * Stays on current Thread
	 *
	 * @param runnable the runnable, that should be executed synchronized
	 */
	@APILevel
	public static void runSynchronized(final Runnable runnable) {
		parameterNotNull(runnable);
		try {
			synchronization.acquire();
			runnable.run();
		} catch (InterruptedException e) {
			logging.catching(e);
		} finally {
			synchronization.release();
		}
	}

	/**
	 * Creates a new {@link NetComThreadFactory}, which is normally not instantiable.
	 * <p>
	 * The NetComThreadFactory is used for {@link ExecutorService}, that should produce {@link NetComThread}.
	 *
	 * @return a new Instance of the {@link NetComThreadFactory}
	 */
	@APILevel
	public static NetComThreadFactory createNewDaemonThreadFactory() {
		return new NetComThreadFactory();
	}

	/**
	 * Returns an instance of {@link NetComThreadFactory}, which is created statically
	 * <p>
	 * The NetComThreadFactory is used for {@link ExecutorService}, that should produce {@link NetComThread}.
	 * <p>
	 * This instance is created once and never updated nor deleted. This is used for saving resources.
	 *
	 * @return a new Instance of the {@link NetComThreadFactory}
	 */
	@APILevel
	public static ThreadFactory getThreadFactory() {
		return THREAD_FACTORY;
	}

	/**
	 * Creates a new cached ExecutorService, based on the {@link NetComThreadFactory}.
	 *
	 * @return a new Instance of the ExecutorService.
	 */
	@APILevel
	public static ExecutorService createNewCachedExecutorService() {
		return Executors.newCachedThreadPool(getThreadFactory());
	}

	/**
	 * Returns a unified instances of an CachedExecutorService, based on the {@link NetComThreadFactory}.
	 * <p>
	 * This instance is created once and never updated nor deleted. This is used for saving resources.
	 *
	 * @return a new Instance of the ExecutorService.
	 */
	@APILevel
	public static ExecutorService getNetComExecutorService() {
		return NET_COM_THREAD;
	}

	/**
	 * Returns whether or not the CurrentThread is an {@link NetComThread}.
	 * <p>
	 * Since there can be more than one NetComThread, this method does a class type check
	 *
	 * @return true, if the current Thread is a NetComThread.
	 */
	@APILevel
	public static boolean onNetComThread() {
		return Thread.currentThread().getClass().equals(NetComThread.class);
	}

	/**
	 * Executes an runnable, but synchronized synchronizes over an Semaphore.
	 * <p>
	 * Leaves the runnable to be executed by the {@link #NET_COM_THREAD} once it is ready.
	 * <p>
	 * The time this is finished cannot be determined. It depends on how full the runnable queue is and whether
	 * or not the {@link #NET_COM_THREAD} is blocked or not.
	 * <p>
	 * Does not wait until the current Thread finishes.
	 *
	 * @param runnable the runnable, that should be executed synchronized
	 */
	@Asynchronous
	public static void runLaterSynchronized(final Runnable runnable) {
		parameterNotNull(runnable);
		runLater(() -> {
			try {
				runnableQueue.put(runnable);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		});
	}

	/**
	 * Executes an runnable, with an indefinite time until execution.
	 * <p>
	 * Leaves the runnable to be executed by the {@link #NET_COM_THREAD} once it is ready.
	 * <p>
	 * The time this is finished cannot be determined. It depends on whether or not the {@link #NET_COM_THREAD} is blocked or not.
	 * <p>
	 * Does wait until the current Thread finishes. So if you depend on the finish of this Runnable, DO NOT USE THIS METHOD!
	 *
	 * @param runnable the runnable, that should be executed synchronized
	 */
	@Asynchronous
	public static void runLater(final Runnable runnable) {
		parameterNotNull(runnable);
		Thread currentThread = Thread.currentThread();
		NET_COM_THREAD.execute(() -> {
			try {
				logging.trace("Awaiting for " + currentThread.getName() + " to finish..");
				currentThread.join();
				logging.trace(currentThread.getName() + "finished. Continue...");
			} catch (InterruptedException e) {
				logging.catching(e);
			}
			runnable.run();
		});
	}

	/**
	 * Executes an runnable, on the NetComThread.
	 * <p>
	 * Leaves the runnable to be executed by the {@link #NET_COM_THREAD} once it is ready.
	 * <p>
	 * The time this is finished cannot be determined. It depends on whether or not the {@link #NET_COM_THREAD} is blocked or not.
	 * <p>
	 * If this Method is executed on the NetComThread, the runnable will be executed immediately.
	 *
	 * @param runnable the runnable, that should be executed synchronized
	 */
	@Asynchronous
	public static void runOnNetComThread(final Runnable runnable) {
		parameterNotNull(runnable);
		if (onNetComThread()) {
			logging.trace("On NetComThread. Running now..");
			runnable.run();
		} else {
			logging.trace("Extracting provided runnable (" + runnable + ") into a NetComThread.");
			NET_COM_THREAD.execute(runnable);
		}
	}

	/**
	 * Creates an ThreadSafe Iterator over the provided Collection
	 *
	 * @param of  the Collection, that the Iterator should be iterated over
	 * @param <T> the type of the Collection
	 * @return an ThreadSafe iterator
	 */
	public static <T> Iterator<T> createAsynchronousIterator(final Collection<T> of) {
		parameterNotNull(of);
		return new AsynchronousIterator<>(of);
	}

	/**
	 * Creates an ThreadSafe Iterator over the provided Collection
	 * <p>
	 * If true is passed in as the <code>removeAllowed</code> the Iterator will use its {@link Iterator#remove()} on the
	 * provided Collection.
	 *
	 * @param of            the Collection, that the Iterator should be iterated over
	 * @param removeAllowed whether or not the iterator should be allowed to remove elements from the provided Collection
	 * @param <T>           the type of the Collection
	 * @return an ThreadSafe iterator
	 */
	public static <T> Iterator<T> createAsynchronousIterator(final Collection<T> of, boolean removeAllowed) {
		parameterNotNull(of);
		return new AsynchronousIterator<>(of, removeAllowed);
	}

	/**
	 * Creates a ThreadFactory that of which all Threads are nonDaemon.
	 * <p>
	 * By Default, all NetComThreads are daemon.
	 *
	 * @return a new ThreadFactory
	 */
	public static ThreadFactory createNewNonDaemonThreadFactory() {
		logging.trace("Creating a new NetComThreadFactory(non-daemon)");
		NetComThreadFactory threadFactory = createNewDaemonThreadFactory();
		threadFactory.setDaemon(false);

		return threadFactory;
	}

	/**
	 * Creates a ThreadFactory that of which all Threads are nonDaemon.
	 * <p>
	 * By Default, all NetComThreads are daemon.
	 *
	 * @return a new ThreadFactory
	 */
	public static ExecutorService createNewNonDaemonExecutorService() {
		return Executors.newCachedThreadPool(NON_DAEMON_THREAD_FACTORY);
	}
}