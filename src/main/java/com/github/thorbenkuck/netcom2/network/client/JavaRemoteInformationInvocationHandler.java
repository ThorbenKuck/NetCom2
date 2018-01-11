package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.remoteObjects.IgnoreRemoteExceptions;
import com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@APILevel
class JavaRemoteInformationInvocationHandler implements InvocationHandler {

	private final Sender sender;
	@APILevel private final RemoteAccessBlockRegistration remoteAccessBlockRegistration;
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

	/**
	 * Processes a method invocation on a proxy instance and returns
	 * the result.  This method will be invoked on an invocation handler
	 * when a method is invoked on a proxy instance that it is
	 * associated with.
	 *
	 * @param proxy  the proxy instance that the method was invoked on
	 * @param method the {@code Method} instance corresponding to
	 *               the interface method invoked on the proxy instance.  The declaring
	 *               class of the {@code Method} object will be the interface that
	 *               the method was declared in, which may be a superinterface of the
	 *               proxy interface that the proxy class inherits the method through.
	 * @param args   an array of objects containing the values of the
	 *               arguments passed in the method invocation on the proxy instance,
	 *               or {@code null} if interface method takes no arguments.
	 *               Arguments of primitive types are wrapped in instances of the
	 *               appropriate primitive wrapper class, such as
	 *               {@code java.lang.Integer} or {@code java.lang.Boolean}.
	 * @return the value to return from the method invocation on the
	 * proxy instance.  If the declared return type of the interface
	 * method is a primitive type, then the value returned by
	 * this method must be an instance of the corresponding primitive
	 * wrapper class; otherwise, it must be a type assignable to the
	 * declared return type.  If the value returned by this method is
	 * {@code null} and the interface method's return type is
	 * primitive, then a {@code NullPointerException} will be
	 * thrown by the method invocation on the proxy instance.  If the
	 * value returned by this method is otherwise not compatible with
	 * the interface method's declared return type as described above,
	 * a {@code ClassCastException} will be thrown by the method
	 * invocation on the proxy instance.
	 */
	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		RemoteAccessCommunicationModelRequest request = new RemoteAccessCommunicationModelRequest(method.getName(), clazz, uuid, args);
		Semaphore semaphore = remoteAccessBlockRegistration.await(request);

		sender.objectToServer(request);

		semaphore.acquire();
		RemoteAccessCommunicationModelResponse response = remoteAccessBlockRegistration.getResponse(uuid);
		remoteAccessBlockRegistration.clearResult(uuid);
		remoteAccessBlockRegistration.clearSemaphore(uuid);
		semaphore.release();

		if(response.getThrownThrowable() != null) {
			testForThrow(clazz, new RemoteRequestException(response.getThrownThrowable()));
		}

		return response.getResult();
	}

	private void testForThrow(Class<?> clazz, Throwable throwable) throws Throwable {
		if(throwable == null) {
			return;
		}

		IgnoreRemoteExceptions annotation = clazz.getAnnotation(IgnoreRemoteExceptions.class);
		if(annotation != null) {
			Class<? extends Throwable>[] toThrowAnyways = annotation.exceptTypes();
			if(Arrays.asList(toThrowAnyways).contains(throwable.getClass())) {
				throw throwable;
			}
		} else {
			throw throwable;
		}
	}
}
