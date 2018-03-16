package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.remoteObjects.IgnoreRemoteExceptions;
import com.github.thorbenkuck.netcom2.exceptions.RemoteObjectNotRegisteredException;
import com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException;
import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class JavaRemoteInformationInvocationHandler<T> implements RemoteObjectHandler {

	private final Sender sender;
	private final RemoteAccessBlockRegistration remoteAccessBlockRegistration;
	private final Class<?> clazz;
	private final UUID uuid;
	private Runnable fallbackRunnable;
	private T fallbackInstance;

	@APILevel
	JavaRemoteInformationInvocationHandler(final Sender sender, final RemoteAccessBlockRegistration remoteAccessBlockRegistration,
										   final Class<T> clazz, final UUID uuid) {
		this.sender = sender;
		this.remoteAccessBlockRegistration = remoteAccessBlockRegistration;
		this.clazz = clazz;
		this.uuid = uuid;
	}

	private Object testForThrow(Class<?> clazz, Method method, Throwable throwable, Object[] args) throws Throwable {
		if (throwable == null) {
			return null;
		}

		if (throwable instanceof RemoteObjectNotRegisteredException) {
			return executeFallback(throwable, method, args);
		}

		IgnoreRemoteExceptions annotation = method.getAnnotation(IgnoreRemoteExceptions.class);
		if (annotation == null) {
			annotation = clazz.getAnnotation(IgnoreRemoteExceptions.class);
		}
		if (annotation != null) {
			Class<? extends Throwable>[] toThrowAnyways = annotation.exceptTypes();
			if (Arrays.asList(toThrowAnyways).contains(throwable.getClass())) {
				throwEncapsulated(throwable);
			}
		} else {
			throwEncapsulated(throwable);
		}

		// This is unessential, but needed,
		// so that the compiler is okay with that.
		// we will ALWAYS have thrown something here.
		return null;
	}

	private void throwEncapsulated(Throwable throwable) throws Throwable {
		List<Throwable> causes = new ArrayList<>();
		Throwable currentCause = throwable.getCause();
		while (currentCause != null) {
			causes.add(currentCause);
			currentCause = currentCause.getCause();
		}
		if (! (throwable instanceof RemoteRequestException)) {
			throwable = new RemoteRequestException("Throwable(" + throwable.getClass().getName() + ") received from Server: " + throwable.getMessage());
		}

		for (Throwable cause : causes) {
			throwable.addSuppressed(cause);
		}

		throw throwable;
	}

	/**
	 * This invoke method, requests computation from the ServerStart.
	 * <p>
	 * It passes the Method, that is requested to be called, and wraps it, so that the parameters are contained, as well
	 * as the Class, which holds the Method.
	 * <p>
	 * It than blocks, until an response is received from the Server, which contains the computed Result or an Exception.
	 * The Result may be null.
	 * If any Exception is send from the Server, this Exception will be rethrown.
	 * <p>
	 * Whether or not an Exception will be thrown, is controlled by the {@link IgnoreRemoteExceptions} annotation.
	 * <p>
	 * Since the computations are done by the Server, any CastException or wrong data type is filtered and replaced by an
	 * corresponding Exception. This might be ignored, in which case <code>null</code> is returned.
	 * <p>
	 * {@inheritDoc}
	 *
	 * @see JavaRemoteInformationInvocationHandler
	 * @see IgnoreRemoteExceptions
	 */
	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		RemoteAccessCommunicationRequest request = new RemoteAccessCommunicationRequest(method.getName(), clazz, uuid, args);

		try {
			sender.objectToServer(request);
		} catch (SendFailedException e) {
			return executeFallback(e, method, args);
		}

		Semaphore semaphore = remoteAccessBlockRegistration.await(request);
		semaphore.acquire();
		RemoteAccessCommunicationResponse response = remoteAccessBlockRegistration.getResponse(uuid);
		remoteAccessBlockRegistration.clearResult(uuid);
		remoteAccessBlockRegistration.clearSemaphore(uuid);
		semaphore.release();

		if (response.getThrownThrowable() != null) {
			return testForThrow(clazz, method, response.getThrownThrowable(), args);
		}

		return response.getResult();
	}

	public void setFallbackRunnable(Runnable fallbackRunnable) {
		synchronized (this) {
			this.fallbackRunnable = fallbackRunnable;
			this.fallbackInstance = null;
		}
	}

	public <S extends T> void setFallbackInstance(S fallbackInstance) {
		synchronized (this) {
			this.fallbackInstance = fallbackInstance;
			this.fallbackRunnable = null;
		}
	}

	protected Object executeFallback(Throwable received, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		synchronized (this) {
			if (fallbackInstance != null) {
				return method.invoke(fallbackInstance, args);
			} else if (fallbackRunnable != null) {
				fallbackRunnable.run();
				return null;
			} else {
				throw new RemoteObjectNotRegisteredException(received.getMessage());
			}
		}
	}
}
