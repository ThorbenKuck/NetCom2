package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.remoteObjects.SingletonRemoteObject;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.Requirements;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@APILevel
class RemoteObjectFactory {

	private final RemoteAccessBlockRegistration remoteAccessBlockRegistration = new RemoteAccessBlockRegistration();
	private final Semaphore invocationHandlerProducerMutex = new Semaphore(1);
	private final Logging logging = Logging.unified();
	private final Map<Class<?>, InvocationHandler> singletons = new HashMap<>();
	private InvocationHandlerProducer invocationHandlerProducer;

	@APILevel
	RemoteObjectFactory(@APILevel final Sender sender) {
		invocationHandlerProducer = new JavaInvocationHandlerProducer(sender, remoteAccessBlockRegistration);
	}

	private InvocationHandler produceInvocationHandler(Class<?> clazz) {
		SingletonRemoteObject singletonRemoteObject = clazz.getAnnotation(SingletonRemoteObject.class);
		if(singletonRemoteObject != null) {
			logging.trace("Detected SingletonRemoteObject request for " + clazz);
			return produceSingleton(clazz);
		} else {
			return produceNew(clazz);
		}
	}

	private InvocationHandler produceSingleton(Class<?> clazz) {
		singletons.computeIfAbsent(clazz, this::produceNew);
		return singletons.get(clazz);
	}

	private InvocationHandler produceNew(Class<?> clazz) {
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

	@APILevel
	@SuppressWarnings("unchecked")
	<T> T createRemoteObject(Class<T> clazz) {
		Requirements.parameterNotNull(clazz);
		InvocationHandler invocationHandler = produceInvocationHandler(clazz);

		if(invocationHandler == null) {
			logging.warn("The provided InvocationHandlerProducer appears to be faulty! Please check the InvocationHandlerProducer" + invocationHandlerProducer + "!");
			throw new IllegalStateException("InvocationHandler is null! This cannot be recovered!");
		}

		return (T) Proxy.newProxyInstance(RemoteObjectFactory.class.getClassLoader(), new Class[]{clazz}, invocationHandler);
	}

	@APILevel
	RemoteAccessBlockRegistration getRemoteAccessBlockRegistration() {
		return remoteAccessBlockRegistration;
	}

	@APILevel
	void setInvocationHandlerProducer(InvocationHandlerProducer producer) throws InterruptedException {
		Requirements.assertNotNull(producer);
		try {
			invocationHandlerProducerMutex.acquire();
			invocationHandlerProducer = producer;
		} finally {
			invocationHandlerProducerMutex.release();
		}
	}

}
