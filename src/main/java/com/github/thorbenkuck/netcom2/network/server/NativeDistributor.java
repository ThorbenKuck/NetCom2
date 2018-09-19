package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import com.github.thorbenkuck.netcom2.network.shared.cache.GeneralCacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class NativeDistributor implements Distributor {

	private static final List<Session> EMPTY_REGISTRATION_ENTRY = Collections.unmodifiableList(new ArrayList<>());
	private final Logging logging = Logging.unified();
	private final DistributorCacheObserver observer = new DistributorCacheObserver();
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

	private void toAllRegistered(Class<?> type, Object object) {
		List<Session> targets;
		synchronized (registrations) {
			targets = new ArrayList<>(registrations.getOrDefault(type, EMPTY_REGISTRATION_ENTRY));
		}

		targets.forEach(session -> session.send(object));
	}

	private Predicate<Session> combine(Predicate<Session>... predicates) {
		return session -> {
			for (Predicate<Session> predicate : predicates) {
				if (!predicate.test(session)) {
					return false;
				}
			}
			return true;
		};
	}

	/**
	 * Sends the specified object to all clients satisfying <b>all</b> given predicates,
	 * by using their DefaultConnection.
	 *
	 * @param o          The object to send
	 * @param predicates The predicates to filter by
	 */
	@Override
	public void toSpecific(Object o, Predicate<Session>... predicates) {
		toSpecific(o, combine(predicates));
	}

	/**
	 * Sends the specified object to all clients satisfying <b>all</b> given predicates,
	 * by using their DefaultConnection.
	 *
	 * @param o         The object to send
	 * @param predicate The predicates to filter by
	 */
	@Override
	public void toSpecific(Object o, Predicate<Session> predicate) {
		clientList.sessionStream()
				.filter(predicate)
				.forEach(session -> session.send(o));
	}

	@Override
	public void toAll(Object object) {
		clientList.sessionStream()
				.forEach(session -> session.send(object));
	}

	@Override
	public void toAllExcept(Object o, Predicate<Session> predicate) {
		clientList.sessionStream()
				.filter(session -> !predicate.test(session))
				.forEach(session -> session.send(o));
	}

	/**
	 * Sends the specified object to all clients that do <b>not satisfy any</b> of the given predicates.
	 * The sending happens using the clients' DefaultConnection.
	 *
	 * @param o          The object to send
	 * @param predicates The predicates to filter by
	 */
	@Override
	public void toAllExcept(Object o, Predicate<Session>... predicates) {
		toAllExcept(o, combine(predicates));
	}

	/**
	 * Sends the given object to all clients that are identified.
	 * The clients' DefaultConnection will be used.
	 *
	 * @param o The object to send
	 */
	@Override
	public void toAllIdentified(Object o) {
		toAllIdentified(o, (Predicate<Session>) null);
	}

	@Override
	@Deprecated
	public void toAllIdentified(Object o, Predicate<Session>... predicates) {
		toAllIdentified(o, combine(predicates));
	}

	/**
	 * Sends the given object to all clients that are identified <b>and</b> satisfy all predicates.
	 * The clients' DefaultConnection will be used.
	 *
	 * @param o         The object to send
	 * @param predicate The predicate to filter by. May be null (but really, you should not put null here.
	 */
	@Override
	public void toAllIdentified(Object o, Predicate<Session> predicate) {
		Predicate<Session> combinedTest = session -> {
			if (!session.isIdentified()) {
				return false;
			}
			if (predicate != null) {
				return predicate.test(session);
			}

			return true;
		};
		List<Session> toSend = clientList.sessionStream()
				.filter(combinedTest)
				.collect(Collectors.toList());

		toSend.forEach(session -> session.send(o));
	}

	/**
	 * Sends the given object to all clients that are registered.
	 * The clients' DefaultConnection will be used.
	 *
	 * @param o The object to send
	 */
	@Override
	public void toRegistered(Object o) {
		toAllRegistered(o.getClass(), o);
	}

	/**
	 * Sends the given object to all clients that are registered <b>and</b> satisfy all predicates.
	 * The clients' DefaultConnection will be used.
	 *
	 * @param o          The object to send
	 * @param predicates The predicates to filter by
	 * @deprecated This method should conflicts with the general idea of the Registration based communication. Normally,
	 * the method {@link #toAllRegistered(Class, Object)} should be invoked, whenever a new, updated or deleted entry is
	 * found in the cache. Using this Method, would imply that we would have to pre-query the list to send to. This
	 * however should already have been done, by the request.
	 */
	@Override
	@Deprecated
	public void toRegistered(Object o, Predicate<Session>... predicates) {
		logging.error("Distributor#toRegistererd(Object, Predicate<Session>) : This method is not used because of the conflicting use cases! See the Javadoc for more information");
	}

	@Override
	public void setup(ServerStart serverStart) {
		cache = serverStart.cache();
		clientList = serverStart.clientList();
		handleRegistration(serverStart.getCommunicationRegistration());
		cache.addGeneralObserver(observer);
	}

	@Override
	public void close() {
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
				session.send(cacheRegistration);
			}
		}
	}

	private final class CacheUnRegistrationHandler implements OnReceive<CacheUnRegistration> {

		@Override
		public void accept(Session session, CacheUnRegistration cacheUnRegistration) {
			unRegister(session, cacheUnRegistration.getType());
			session.send(cacheUnRegistration);
		}
	}
}