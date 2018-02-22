package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.remoteObjects.IgnoreRemoteExceptions;
import com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationResponse;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@APILevel
class JavaRemoteInformationInvocationHandler implements RemoteObjectHandler {

	private final Sender sender;
	@APILevel
	private final RemoteAccessBlockRegistration remoteAccessBlockRegistration;
	private final Class<?> clazz;
	private final UUID uuid;

	@APILevel
	JavaRemoteInformationInvocationHandler(final Sender sender, final RemoteAccessBlockRegistration remoteAccessBlockRegistration,
										   final Class<?> clazz, final UUID uuid) {
		this.sender = sender;
		this.remoteAccessBlockRegistration = remoteAccessBlockRegistration;
		this.clazz = clazz;
		this.uuid = uuid;
	}

	private void testForThrow(Class<?> clazz, Throwable throwable) throws Throwable {
		if (throwable == null) {
			return;
		}

		IgnoreRemoteExceptions annotation = clazz.getAnnotation(IgnoreRemoteExceptions.class);
		if (annotation != null) {
			Class<? extends Throwable>[] toThrowAnyways = annotation.exceptTypes();
			if (Arrays.asList(toThrowAnyways).contains(throwable.getClass())) {
				throwEncapsulated(throwable);
			}
		} else {
			throwEncapsulated(throwable);
		}
	}

	private void throwEncapsulated(Throwable throwable) throws Throwable {
		if (! (throwable instanceof RemoteRequestException)) {
			throwable = new RemoteRequestException(throwable);
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
		Semaphore semaphore = remoteAccessBlockRegistration.await(request);

		sender.objectToServer(request);

		semaphore.acquire();
		RemoteAccessCommunicationResponse response = remoteAccessBlockRegistration.getResponse(uuid);
		remoteAccessBlockRegistration.clearResult(uuid);
		remoteAccessBlockRegistration.clearSemaphore(uuid);
		semaphore.release();

		if (response.getThrownThrowable() != null) {
			testForThrow(clazz, response.getThrownThrowable());
		}

		return response.getResult();
	}

}
