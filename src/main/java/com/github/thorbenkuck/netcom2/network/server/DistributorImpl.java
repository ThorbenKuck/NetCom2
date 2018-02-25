package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.CachePush;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@APILevel
@Synchronized
class DistributorImpl implements InternalDistributor {

	private final Logging logging = Logging.unified();
	private final ClientList clientList;
	private final DistributorRegistration distributorRegistration;

	@APILevel
	DistributorImpl(final ClientList clientList, final DistributorRegistration distributorRegistration) {
		this.clientList = clientList;
		this.distributorRegistration = distributorRegistration;
	}

	/**
	 * Tests whether the provided Session is suitable by the provided predicates
	 *
	 * @param session    the Session, that should be tested
	 * @param predicates the predicates, that define whether or not the Session is applicable
	 * @return true, if all predicates are applicable, else false
	 */
	@SafeVarargs
	private final boolean testAgainst(final Session session, final Predicate<Session>... predicates) {
		for (final Predicate<Session> predicate : predicates) {
			if (!predicate.test(session)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DistributorRegistration getDistributorRegistration() {
		return distributorRegistration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	@SafeVarargs
	public synchronized final void toSpecific(final Object o, final Predicate<Session>... predicates) {
		final List<Session> clientsToSendTo = new ArrayList<>();
		synchronized (clientList) {
			clientList.sessionStream()
					.filter(user -> testAgainst(user, predicates))
					.forEach(clientsToSendTo::add);
		}
		clientsToSendTo.forEach(session -> session.send(o));
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public final void toAll(final Object o) {
		toSpecific(o, Objects::nonNull);
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@SafeVarargs
	@Override
	public synchronized final void toAllExcept(final Object o, final Predicate<Session>... predicates) {
		final List<Session> toSendTo = new ArrayList<>();
		synchronized (clientList) {
			clientList.sessionStream()
					.filter(user -> !testAgainst(user, predicates))
					.forEach(toSendTo::add);
		}
		toSendTo.forEach(session -> session.send(o));
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public final void toAllIdentified(final Object o) {
		toSpecific(o, Session::isIdentified);
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@SafeVarargs
	@Override
	public final void toAllIdentified(final Object o, final Predicate<Session>... predicates) {
		predicates[predicates.length - 1] = Session::isIdentified;
		toSpecific(o, predicates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public final void toRegistered(final Object o) {
		toRegistered(o, Objects::nonNull);
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	@SafeVarargs
	public final void toRegistered(final Object o, final Predicate<Session>... predicates) {
		final List<Session> toSendTo;
		synchronized (distributorRegistration) {
			toSendTo = distributorRegistration.getRegistered(o.getClass());
		}
		logging.trace("Trying to send " + o + " to " + toSendTo);
		toSendTo.stream()
				.filter(user -> testAgainst(user, predicates))
				.forEach(user -> {
					logging.trace("Sending cache-update at " + o.getClass() + " to " + user);
					user.send(new CachePush(o));
				});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Distributor{" +
				"clientList=" + clientList +
				'}';
	}
}
