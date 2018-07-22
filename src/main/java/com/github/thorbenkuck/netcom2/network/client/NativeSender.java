package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.*;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.DefaultConnection;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

import java.util.HashMap;
import java.util.Map;

class NativeSender implements Sender {

	private final Logging logging = Logging.unified();
	private final Value<Client> clientValue = Value.emptySynchronized();
	private final CacheUpdateHandler cacheUpdateHandler = new CacheUpdateHandler();
	private final CacheAdditionHandler cacheAdditionHandler = new CacheAdditionHandler();
	private final CacheRemoveHandler cacheRemoveHandler = new CacheRemoveHandler();
	private final CacheRegistrationHandler cacheRegistrationHandler = new CacheRegistrationHandler();
	private final CacheUnRegistrationHandler cacheUnRegistrationHandler = new CacheUnRegistrationHandler();
	private final Map<Class<?>, CacheObserver<?>> observerLimbo = new HashMap<>();
	private CommunicationRegistration communicationRegistration;
	private Cache cache;

	NativeSender() {
		logging.instantiated(this);
	}

	private void handleRegistrations() {
		try {
			communicationRegistration.acquire();
			communicationRegistration.register(CacheAddition.class)
					.addFirst(cacheAdditionHandler);
			communicationRegistration.register(CacheUpdate.class)
					.addFirst(cacheUpdateHandler);
			communicationRegistration.register(CacheRemove.class)
					.addFirst(cacheRemoveHandler);
			communicationRegistration.register(CacheRegistration.class)
					.addFirst(cacheRegistrationHandler);
			communicationRegistration.register(CacheUnRegistration.class)
					.addFirst(cacheUnRegistrationHandler);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			communicationRegistration.release();
		}
	}

	/**
	 * Sends an Object over the network to the server.
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns a synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 * <p>
	 * It uses the {@link DefaultConnection}
	 *
	 * @param o the Object that should be send over the network.
	 */
	@Override
	public void objectToServer(Object o) {
		clientValue.get()
				.send(o);
	}

	/**
	 * Sends an Object over the network to the server.
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns a synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 * <p>
	 * It uses the specified Connection.
	 *
	 * @param o          the Object that should be send over the network.
	 * @param connection the Connection that should be used.
	 */
	@Override
	public void objectToServer(Object o, Connection connection) {
		clientValue.get()
				.send(o, connection);
	}

	/**
	 * Sends an Object over the network to the server.
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns a synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 * <p>
	 * It uses the Connection, identified by the provided key.
	 *
	 * @param o             the Object that should be send over the network.
	 * @param connectionKey the identifier of the Connection that should be used
	 */
	@Override
	public void objectToServer(Object o, Class connectionKey) {
		clientValue.get()
				.send(o, connectionKey);
	}

	/**
	 * Sends a register request to the Server. If successful, the Server will update the Client every time, the requested
	 * Object is updated
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns a synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 *
	 * @param clazz    the class of the message, you want to register to
	 * @param observer the callback object, that should be called, if an Object of the specified type arrives
	 */
	@Override
	public <T> void registrationToServer(Class<T> clazz, CacheObserver<T> observer) {
		synchronized (observerLimbo) {
			observerLimbo.put(clazz, observer);
		}
		objectToServer(new CacheRegistration(clazz));
	}

	/**
	 * Sends a register request to the Server. If successful, the Server will update the Client every time, the requested
	 * Object is updated
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns a synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 * <p>
	 * Uses the provided Connection.
	 *
	 * @param clazz      the class of the message, you want to register to
	 * @param observer   the callback object, that should be called, if an Object of the specified type arrives
	 * @param connection the Connection that should be used.
	 */
	@Override
	public <T> void registrationToServer(Class<T> clazz, CacheObserver<T> observer, Connection connection) {
		synchronized (observerLimbo) {
			observerLimbo.put(clazz, observer);
		}
		objectToServer(new CacheRegistration(clazz), connection);
	}

	/**
	 * Sends a register request to the Server. If successful, the Server will update the Client every time, the requested
	 * Object is updated
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns an synchronization mechanism,
	 * that will allow te developer to listen for a successful send or incoming messages.
	 * <p>
	 * Uses the Connection, identified with the provided <code>connectionKey</code>
	 *
	 * @param clazz         the class of the message, you want to register to
	 * @param observer      the callback object, that should be called, if an Object of the specified type arrives
	 * @param connectionKey the key for the Connection that should be used.
	 */
	@Override
	public <T> void registrationToServer(Class<T> clazz, CacheObserver<T> observer, Class connectionKey) {
		synchronized (observerLimbo) {
			observerLimbo.put(clazz, observer);
		}
		objectToServer(new CacheRegistration(clazz), connectionKey);
	}

	/**
	 * Requests an unRegistration from the specified message type.
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns an synchronization mechanism,
	 * that will allow te developer to listen for a successful send or incoming messages.
	 *
	 * @param clazz the class of the message, you want to register to
	 */
	@Override
	public <T> void unRegistrationToServer(Class<T> clazz) {
		objectToServer(new CacheUnRegistration(clazz));
	}

	/**
	 * Requests an unRegistration from the specified message type.
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns an synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 *
	 * @param clazz      the class of the message, you want to register to
	 * @param connection the Connection that should be used
	 */
	@Override
	public <T> void unRegistrationToServer(Class<T> clazz, Connection connection) {
		objectToServer(new CacheUnRegistration(clazz), connection);
	}

	/**
	 * Requests an unRegistration from the specified message type.
	 * <p>
	 * The call will result in an asynchronous extraction. However, it returns an synchronization mechanism,
	 * that will allow the developer to listen for a successful send or incoming messages.
	 *
	 * @param clazz         the class of the message, you want to register to
	 * @param connectionKey the class, identifying the Connection that should be used.
	 */
	@Override
	public <T> void unRegistrationToServer(Class<T> clazz, Class connectionKey) {
		objectToServer(new CacheUnRegistration(clazz), connectionKey);
	}

	/**
	 * Resets the Sender to its original state.
	 * <p>
	 * Calling this Method is not recommended. It is utilized, to reset a Client, once it disconnects from the Server,
	 * to allow it to reconnect. It cleans out all internal saved instances
	 * <p>
	 * Calling this Method results in a clean of all requested registration and there corresponding callback Objects.
	 * So if you send an register Request and shortly after call this method, the callback Object will no longer be available.
	 * <p>
	 * This method is called if the last Connection is disconnected.
	 */
	@Override
	public void reset() {
		synchronized (observerLimbo) {
			observerLimbo.forEach((key, value) -> cache.removeCacheObserver(value));
			observerLimbo.clear();
		}
	}

	@Override
	public synchronized void setup(ClientStart clientStart) {
		if (!clientValue.isEmpty()) {
			throw new IllegalStateException("Already setup!");
		}

		// This cast is ugly. Maybe
		// We can somehow decouple
		// the ClientStart and the
		// Client whilst still exposing
		// this? Dunno..
		clientValue.set(((NativeClientStart) clientStart).getClient());
		communicationRegistration = clientStart.getCommunicationRegistration();
		cache = clientStart.cache();
		handleRegistrations();
	}

	@Override
	public void close() {
		try {
			communicationRegistration.acquire();
			communicationRegistration.register(CacheUpdate.class)
					.remove(cacheUpdateHandler);
			communicationRegistration.register(CacheAddition.class)
					.remove(cacheAdditionHandler);
			communicationRegistration.register(CacheRemove.class)
					.remove(cacheRemoveHandler);
			communicationRegistration.register(CacheRegistration.class)
					.remove(cacheRegistrationHandler);
			communicationRegistration.register(CacheUnRegistration.class)
					.remove(cacheUnRegistrationHandler);
		} catch (InterruptedException e) {
			logging.catching(e);
		} finally {
			communicationRegistration.release();
		}
	}

	private final class CacheAdditionHandler implements OnReceiveSingle<CacheAddition> {

		@Override
		public void accept(CacheAddition cacheAddition) {
			cache.addNew(cacheAddition.getObject());
		}
	}

	private final class CacheUpdateHandler implements OnReceiveSingle<CacheUpdate> {

		@Override
		public void accept(CacheUpdate cacheUpdate) {
			cache.update(cacheUpdate.getObject());
		}
	}

	private final class CacheRemoveHandler implements OnReceiveSingle<CacheRemove> {

		@Override
		public void accept(CacheRemove cacheRemove) {
			cache.remove(cacheRemove.getType());
		}
	}

	private final class CacheRegistrationHandler implements OnReceive<CacheRegistration> {

		@Override
		public void accept(Session session, CacheRegistration cacheRegistration) {
			CacheObserver<?> cacheObserver;
			synchronized (observerLimbo) {
				cacheObserver = observerLimbo.get(cacheRegistration.getType());
			}
			cache.addCacheObserver(cacheObserver);
		}
	}

	private final class CacheUnRegistrationHandler implements OnReceiveSingle<CacheUnRegistration> {

		@Override
		public void accept(CacheUnRegistration cacheUnRegistration) {
			synchronized (observerLimbo) {
				CacheObserver<?> cacheObserver;
				synchronized (observerLimbo) {
					cacheObserver = observerLimbo.get(cacheUnRegistration.getType());
				}
				cache.removeCacheObserver(cacheObserver);
			}
		}
	}
}
