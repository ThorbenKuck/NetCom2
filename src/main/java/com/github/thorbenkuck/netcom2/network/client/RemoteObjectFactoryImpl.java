package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.rmi.SingletonRemoteObject;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@APILevel
class RemoteObjectFactoryImpl implements RemoteObjectFactory {

	private final RemoteAccessBlockRegistration remoteAccessBlockRegistration = new RemoteAccessBlockRegistration();
	private final Semaphore invocationHandlerProducerMutex = new Semaphore(1);
	private final Logging logging = Logging.unified();
	@APILevel
	final Map<Class<?>, JavaRemoteInformationInvocationHandler<?>> singletons = new HashMap<>();
	@APILevel
	final Map<Class<?>, Runnable> fallbackRunnableMap = new HashMap<>();
	@APILevel
	final Map<Class<?>, Object> fallbackInstances = new HashMap<>();
	private Runnable defaultFallback;
	private InvocationHandlerProducer invocationHandlerProducer;

	@APILevel
	RemoteObjectFactoryImpl(@APILevel final Sender sender) {
		invocationHandlerProducer = new JavaInvocationHandlerProducer(sender, remoteAccessBlockRegistration);
	}

	private <T> JavaRemoteInformationInvocationHandler<T> produceInvocationHandler(Class<T> clazz) {
		SingletonRemoteObject singletonRemoteObject = clazz.getAnnotation(SingletonRemoteObject.class);
		JavaRemoteInformationInvocationHandler<T> invocationHandler;
		if (singletonRemoteObject != null) {
			logging.trace("Detected SingletonRemoteObject request for " + clazz);
			invocationHandler = produceSingleton(clazz);
		} else {
			invocationHandler = produceNew(clazz);
		}

		if (invocationHandler == null) {
			logging.warn("The provided InvocationHandlerProducer appears to be faulty! Please check the InvocationHandlerProducer" + invocationHandlerProducer + "!");
			throw new IllegalStateException("InvocationHandler is null! This cannot be recovered!");
		}

		invocationHandler.setFallbackRunnable(defaultFallback);

		// We do check here, for null, so that we do not override
		// the defaultFallback, if it is set correctly.
		// Since null is valid in JavaRemoteInformationInvocationHandler
		// to delete the currently set instances.
		Runnable runnable = fallbackRunnableMap.get(clazz);
		if (runnable != null) {
			invocationHandler.setFallbackRunnable(runnable);
		}
		Object o = fallbackInstances.get(clazz);
		if (o != null && clazz.isAssignableFrom(o.getClass())) {
			invocationHandler.setFallbackInstance((T) o);
		}

		return invocationHandler;
	}

	@SuppressWarnings ("unchecked")
	private <T> JavaRemoteInformationInvocationHandler<T> produceSingleton(Class<T> clazz) {
		singletons.computeIfAbsent(clazz, this::produceNew);
		return (JavaRemoteInformationInvocationHandler<T>) singletons.get(clazz);
	}

	private <T> JavaRemoteInformationInvocationHandler<T> produceNew(Class<T> clazz) {
		logging.trace("Producing new InvocationHandler for " + clazz);
		UUID uuid = createUUID();
		try {
			logging.trace("Acquiring access over InvocationHandlerProducer ..");
			invocationHandlerProducerMutex.acquire();
			logging.trace("Asking the InvocationHandlerProducer to produce a new InvocationHandler ..");
			return invocationHandlerProducer.produce(uuid, clazz);
		} catch (InterruptedException e) {
			logging.error("Could not create Invocation Handler: Semaphore Interrupted while waiting for access over Producer!", e);
			throw new IllegalStateException("Could not acquire access over invocationHandlerProducer!");
		} catch (Exception otherException) {
			logging.error("Encountered unexpected Exception while producing InvocationHandler!", otherException);
			throw new IllegalStateException("Cannot Handle unexpected Exceptions while creating InvocationHandler!", otherException);
		} finally {
			invocationHandlerProducerMutex.release();
		}
	}

	private synchronized UUID createUUID() {
		return UUID.randomUUID();
	}

	@SuppressWarnings ("unchecked")
	private <T> T createRemoteObject(JavaRemoteInformationInvocationHandler<T> invocationHandler, Class<T> clazz) {
		return (T) Proxy.newProxyInstance(RemoteObjectFactoryImpl.class.getClassLoader(), new Class[] { clazz }, invocationHandler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaultFallback(Runnable runnable) {
		this.defaultFallback = runnable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFallback(Class<?> clazz, Runnable runnable) {
		synchronized (fallbackRunnableMap) {
			fallbackRunnableMap.put(clazz, runnable);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T, S extends T> void setFallbackInstance(Class<T> clazz, S instance) {
		synchronized (fallbackInstances) {
			fallbackInstances.put(clazz, instance);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T create(Class<T> type) {
		return createRemoteObject(type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T create(Class<T> type, Runnable customFallback) {
		JavaRemoteInformationInvocationHandler<T> invocationHandler = produceInvocationHandler(type);
		invocationHandler.setFallbackRunnable(customFallback);

		return createRemoteObject(invocationHandler, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T create(Class<T> type, T fallbackInstance) {
		JavaRemoteInformationInvocationHandler<T> invocationHandler = produceInvocationHandler(type);
		invocationHandler.setFallbackInstance(fallbackInstance);

		return createRemoteObject(invocationHandler, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T createWithoutFallback(Class<T> type) {
		JavaRemoteInformationInvocationHandler<T> invocationHandler = produceInvocationHandler(type);
		invocationHandler.setFallbackRunnable(null);

		return createRemoteObject(invocationHandler, type);
	}

	<T> T createRemoteObject(Class<T> clazz, Runnable fallback) {
		NetCom2Utils.parameterNotNull(clazz, fallback);
		JavaRemoteInformationInvocationHandler<T> invocationHandler = produceInvocationHandler(clazz);

		invocationHandler.setFallbackRunnable(fallback);

		return createRemoteObject(invocationHandler, clazz);
	}

	<T, S extends T> T createRemoteObject(Class<T> clazz, S instance) {
		NetCom2Utils.parameterNotNull(clazz);
		JavaRemoteInformationInvocationHandler<T> invocationHandler = produceInvocationHandler(clazz);

		invocationHandler.setFallbackInstance(instance);

		return createRemoteObject(invocationHandler, clazz);
	}

	@APILevel
	@SuppressWarnings ("unchecked")
	<T> T createRemoteObject(Class<T> clazz) {
		NetCom2Utils.parameterNotNull(clazz);
		JavaRemoteInformationInvocationHandler<T> invocationHandler = produceInvocationHandler(clazz);

		if (invocationHandler == null) {
			logging.warn("The provided InvocationHandlerProducer appears to be faulty! Please check the InvocationHandlerProducer" + invocationHandlerProducer + "!");
			throw new IllegalStateException("InvocationHandler is null! This cannot be recovered!");
		}

		return createRemoteObject(invocationHandler, clazz);
	}

	@APILevel
	RemoteAccessBlockRegistration getRemoteAccessBlockRegistration() {
		return remoteAccessBlockRegistration;
	}

	@APILevel
	void setInvocationHandlerProducer(InvocationHandlerProducer producer) throws InterruptedException {
		NetCom2Utils.assertNotNull(producer);
		try {
			invocationHandlerProducerMutex.acquire();
			invocationHandlerProducer = producer;
		} finally {
			invocationHandlerProducerMutex.release();
		}
	}
}
