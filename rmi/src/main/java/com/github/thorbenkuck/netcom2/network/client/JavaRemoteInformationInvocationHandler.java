package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.rmi.IgnoreRemoteExceptions;
import com.github.thorbenkuck.netcom2.exceptions.RemoteObjectNotRegisteredException;
import com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException;
import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.comm.RemoteAccessCommunicationRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.RemoteAccessCommunicationResponse;

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
	private final Logging logging = Logging.unified();
	private Runnable fallbackRunnable;
	private T fallbackInstance;

	@APILevel
	JavaRemoteInformationInvocationHandler(final Sender sender, final RemoteAccessBlockRegistration remoteAccessBlockRegistration,
										   final Class<T> clazz, final UUID uuid) {
		this.sender = sender;
		this.remoteAccessBlockRegistration = remoteAccessBlockRegistration;
		this.clazz = clazz;
		this.uuid = uuid;
		logging.instantiated(this);
	}

	/**
	 * Tests whether or not the provided throwable should be thrown.
	 * <p>
	 * This method checks multiple things, including checking for the {@link IgnoreRemoteExceptions} annotation, which
	 * might be put at the method, or the Class.
	 *
	 * @param clazz     the class, which is proxied.
	 * @param method    the Method that was called.
	 * @param throwable the throwable, that was encountered
	 * @param args      the passed arguments.
	 * @return an Object, but only if the Throwable should not be thrown
	 * @throws Throwable if the Throwable should be thrown.
	 */
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

		// This return value is simply
		// for the case, that nothing
		// is thrown. This will be
		// provided to the Client.
		return null;
	}

	/**
	 * This Method will take an Throwable and encapsulates it within an {@link RemoteRequestException}.
	 *
	 * @param throwable the Throwable, that should be encapsulated
	 * @throws Throwable the provided Throwable, encapsulated within an RemoteRequestException
	 */
	private void throwEncapsulated(Throwable throwable) throws Throwable {
		List<Throwable> causes = new ArrayList<>();
		Throwable currentCause = throwable.getCause();
		while (currentCause != null) {
			causes.add(currentCause);
			currentCause = currentCause.getCause();
		}
		if (!(throwable instanceof RemoteRequestException)) {
			throwable = new RemoteRequestException("Throwable(" + throwable.getClass().getName() + ") received from Server: " + throwable.getMessage());
		}

		for (Throwable cause : causes) {
			throwable.addSuppressed(cause);
		}

		throw throwable;
	}

	/**
	 * Executes the set Fallbacks.
	 * <p>
	 * If no Fallback is accessible, a {@link RemoteObjectNotRegisteredException} will be thrown instead
	 * <p>
	 * The return value depends on the set fallback. It will return:
	 * <p>
	 * <ul>
	 * <li>A correct instance, if the Fallback is an instance</li>
	 * <li>null, if the fallback is an runnable</li>
	 * <li>nothing, else. In this case, an RemoteObjectNotRegisteredException will be thrown.</li>
	 * </ul>
	 *
	 * @param received the Throwable received from the RemoteObject (might be null)
	 * @param method   the Method, that was invoked
	 * @param args     the Arguments, passed to the method-call
	 * @return the Result of the fallback execution. Might be null!
	 * @throws InvocationTargetException if the method within the fallback instance is not correctly invokable
	 * @throws IllegalAccessException    if the fallback instance has changed the access rights to the method
	 */
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

	/**
	 * This invoke method, requests computation from the ServerStart.
	 * <p>
	 * It passes the Method, that is requested to be called, and wraps it, so that the parameters are contained, as well
	 * as the Class, which holds the Method.
	 * <p>
	 * It then blocks, until a response is received from the Server, which contains the computed Result or an Exception.
	 * The Result may be null.
	 * If any Exception is send from the Server, this Exception will be rethrown.
	 * <p>
	 * Whether or not an Exception will be thrown, is controlled by the {@link IgnoreRemoteExceptions} annotation.
	 * <p>
	 * Since the computations are done by the Server, any CastException or wrong data type is filtered and replaced by a
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

	/**
	 * Sets the Runnable, that should be run, if the RemoteObject is not accessible.
	 *
	 * @param fallbackRunnable the Runnable
	 */
	public void setFallbackRunnable(Runnable fallbackRunnable) {
		synchronized (this) {
			this.fallbackRunnable = fallbackRunnable;
			this.fallbackInstance = null;
		}
	}

	/**
	 * Sets an Instance, that should be run, if the RemoteObject is not accessible.
	 * <p>
	 * Whenever the RemoteObject is not accessible, this InvocationHandler will attempt to call the provided instance, until
	 * the RemoteObject is accessible again.
	 *
	 * @param fallbackInstance the Runnable
	 * @param <S>              the type of the fallbackInstance, which must extend the Type of the Proxy.
	 */
	public <S extends T> void setFallbackInstance(S fallbackInstance) {
		synchronized (this) {
			this.fallbackInstance = fallbackInstance;
			this.fallbackRunnable = null;
		}
	}

}
