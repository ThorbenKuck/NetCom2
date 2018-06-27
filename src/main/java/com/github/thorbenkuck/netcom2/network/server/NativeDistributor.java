package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import com.github.thorbenkuck.netcom2.network.shared.cache.GeneralCacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.*;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

import java.util.*;

class NativeDistributor implements Distributor {

	private final Logging logging = Logging.unified();
	private final DistributorCacheObserver observer = new DistributorCacheObserver();
	private final List<Session> emptyRegistrationEntry = Collections.unmodifiableList(new ArrayList<>());
	private final Map<Class<?>, List<Session>> registrations = new HashMap<>();
	private final CacheRegistrationHandler cacheRegistrationHandler = new CacheRegistrationHandler();
	private final CacheUnRegistrationHandler cacheUnRegistrationHandler = new CacheUnRegistrationHandler();
	private Cache cache;
	private ClientList clientList;
	private CommunicationRegistration communicationRegistration;

	NativeDistributor() {
		logging.instantiated(this);
	}

	private void handleRegistration(CommunicationRegistration communicationRegistration) {
		try {
			communicationRegistration.acquire();
			communicationRegistration.register(CacheRegistration.class)
					.addFirst(cacheRegistrationHandler);
			communicationRegistration.register(CacheUnRegistration.class)
					.addFirst(cacheUnRegistrationHandler);
			this.communicationRegistration = communicationRegistration;
		} catch (InterruptedException e) {
			logging.catching(e);
		} finally {
			communicationRegistration.release();
		}
	}

	private void register(Session session, Class<?> type) {
		synchronized (registrations) {
			registrations.computeIfAbsent(type, clazz -> new ArrayList<>());
			registrations.get(type).add(session);
		}
	}

	private void unRegister(Session session, Class<?> type) {
		synchronized (registrations) {
			List<Session> registered = registrations.get(type);
			if (registered != null) {
				registered.remove(session);
				if (registered.isEmpty()) {
					registrations.remove(type);
				}
			}
		}
	}

	public void toAll(Object object) {
		clientList.snapShot()
				.forEach(client -> client.send(object));
	}

	public void toAllRegistered(Class<?> type, Object object) {
		List<Session> targets;
		synchronized (registrations) {
			targets = new ArrayList<>(registrations.getOrDefault(type, emptyRegistrationEntry));
		}
		// TODO create response "Data" object
		// TODO remove null for data object
		targets.forEach(session -> session.send(object));
	}

	@Override
	public void setup(ServerStart serverStart) {
		cache = serverStart.cache();
		clientList = serverStart.clientList();
		handleRegistration(serverStart.getCommunicationRegistration());
		cache.addGeneralObserver(observer);
	}

	@Override
	public void shutdown() {
		cache.removeGeneralObserver(observer);
		try {
			communicationRegistration.acquire();
			// TODO Extract
			ReceivePipeline<CacheRegistration> pipeline = communicationRegistration.register(CacheRegistration.class);
			pipeline.remove(cacheRegistrationHandler);
			if (pipeline.isEmpty()) {
				communicationRegistration.unRegister(CacheRegistration.class);
			}
			ReceivePipeline<CacheUnRegistration> unRegistrationPipeline = communicationRegistration.register(CacheUnRegistration.class);
			unRegistrationPipeline.remove(cacheUnRegistrationHandler);
			if (unRegistrationPipeline.isEmpty()) {
				communicationRegistration.unRegister(CacheUnRegistration.class);
			}
		} catch (InterruptedException e) {
			logging.catching(e);
		} finally {
			communicationRegistration.release();
		}
	}

	private final class DistributorCacheObserver implements GeneralCacheObserver {

		@Override
		public void newEntry(Object object, CacheObservable observable) {
			toAllRegistered(object.getClass(), new CacheAddition(object));
		}

		@Override
		public void updatedEntry(Object object, CacheObservable observable) {
			toAllRegistered(object.getClass(), new CacheUpdate(object));
		}

		@Override
		public void deletedEntry(Object object, CacheObservable observable) {
			toAllRegistered(object.getClass(), new CacheRemove(object.getClass()));
		}
	}

	private final class CacheRegistrationHandler implements OnReceive<CacheRegistration> {

		@Override
		public void accept(Session session, CacheRegistration cacheRegistration) {
			List<Session> registered;

			synchronized (registrations) {
				registered = new ArrayList<>(registrations.getOrDefault(cacheRegistration.getType(), new ArrayList<>()));
			}

			if (!registered.contains(session)) {
				register(session, cacheRegistration.getType());
			}
		}
	}

	private final class CacheUnRegistrationHandler implements OnReceive<CacheUnRegistration> {

		@Override
		public void accept(Session session, CacheUnRegistration cacheUnRegistration) {
			unRegister(session, cacheUnRegistration.getType());
		}
	}
}