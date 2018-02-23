package com.github.thorbenkuck.netcom2.utility;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.pipeline.Wrapper;

import java.util.concurrent.*;

/**
 * This Utility Class defines some methods, that are commonly used within NetCom2
 * <p>
 * Since those checks are stateless, they are static.
 * Thread-Safety should not be necessary, else whatever needs Thread-Safety does not belong here
 * <p>
 * Since this class is used mainly internally, it should not be used outside of the internal project.
 * If you want to use it anyways, use it with care! This class is highly likely to be subject of change!
 */
@APILevel
public class NetCom2Utils {

	/*
	 * The following is used internally
	 */
	private static final Logging logging = Logging.unified();
	private static final Wrapper wrapper = new Wrapper();
	/*
	 * The following is needed, for the asynchronous API
	 * it defines multiple Thread-necessities, as well as
	 * an custom ThreadFactory.
	 */
	private static final BlockingQueue<Runnable> runnableQueue = new LinkedBlockingQueue<>();
	private static final ThreadFactory threadFactory = createNewThreadFactory();
	private static final ExecutorService queueExecutorService = Executors.newSingleThreadExecutor(threadFactory);
	private static final ExecutorService netComThread = createNewCachedExecutorService();

	static {
		queueExecutorService.execute(() -> {
			try {
				Runnable runnable = runnableQueue.take();
				assertNotNull(runnable);
				runnable.run();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
		for (final Object object : objects) {
			parameterNotNull(object);
		}
	}

	/**
	 * This Method is a short form for {@link Wrapper#wrap(OnReceiveSingle)} and uses an static instance of <code>Wrapper</code>
	 * <p>
	 * This means you do not need to create <code>Wrapper</code> instances, whenever an <code>OnReceive</code> should be wrapped
	 *
	 * @param onReceiveSingle the {@link OnReceiveSingle} that should be wrapped
	 * @param <T>             the generic Type of the OnReceiveSingle
	 * @return a wrapped {@link OnReceiveTriple}, that is used internally of the {@link com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline}
	 * @see com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline
	 * @see #wrap(OnReceive)
	 */
	@APILevel
	public static <T> OnReceiveTriple<T> wrap(OnReceiveSingle<T> onReceiveSingle) {
		return wrapper.wrap(onReceiveSingle);
	}

	/**
	 * This Method is a short form for {@link Wrapper#wrap(OnReceive)} and uses an static instance of <code>Wrapper</code>
	 * <p>
	 * This means you do not need to create <code>Wrapper</code> instances, whenever an <code>OnReceive</code> should be wrapped
	 *
	 * @param onReceive the {@link OnReceive} that should be wrapped
	 * @param <T>       the generic Type of the OnReceive
	 * @return a wrapped {@link OnReceiveTriple}, that is used internally of the {@link com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline}
	 * @see com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline
	 * @see #wrap(OnReceiveSingle)
	 */
	@APILevel
	public static <T> OnReceiveTriple<T> wrap(OnReceive<T> onReceive) {
		return wrapper.wrap(onReceive);
	}

	@APILevel
	public static void runSynchronized(final Runnable runnable) {
		parameterNotNull(runnable);
		synchronized (runnableQueue) {
			runnable.run();
		}
	}

	@APILevel
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

	@APILevel
	public static void runLater(final Runnable runnable) {
		parameterNotNull(runnable);
		Thread currentThread = Thread.currentThread();
		netComThread.execute(() -> {
			try {
				currentThread.join();
			} catch (InterruptedException e) {
				logging.catching(e);
			}
			runnable.run();
		});
	}

	public static void runOnNetComThread(final Runnable runnable) {
		parameterNotNull(runnable);
		if (onNetComThread()) {
			runnable.run();
		} else {
			runLater(runnable);
		}
	}

	public static ThreadFactory createNewThreadFactory() {
		return new NetComThreadFactory();
	}

	public static ThreadFactory getThreadFactory() {
		return threadFactory;
	}

	public static ExecutorService createNewCachedExecutorService() {
		return Executors.newCachedThreadPool(getThreadFactory());
	}

	public static ExecutorService getNetComExecutorService() {
		return netComThread;
	}

	public static boolean onNetComThread() {
		Thread current = Thread.currentThread();

		return current.getName().equals(NetComThreadFactory.NET_COM_THREAD_NAME);
	}
}