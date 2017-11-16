package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.CachePush;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Synchronized
class DistributorImpl implements InternalDistributor {

	private final Logging logging = Logging.unified();
	private final ClientList clientList;
	private final DistributorRegistration distributorRegistration;

	DistributorImpl(final ClientList clientList, final DistributorRegistration distributorRegistration) {
		this.clientList = clientList;
		this.distributorRegistration = distributorRegistration;
	}

	@Override
	public final DistributorRegistration getDistributorRegistration() {
		return distributorRegistration;
	}

	@Override
	public String toString() {
		return "Distributor{" +
				"clientList=" + clientList +
				'}';
	}

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

	@Asynchronous
	@Override
	public final void toAllIdentified(final Object o) {
		toSpecific(o, Session::isIdentified);
	}

	@Asynchronous
	@SafeVarargs
	@Override
	public final void toAllIdentified(final Object o, final Predicate<Session>... predicates) {
		predicates[predicates.length - 1] = Session::isIdentified;
		toSpecific(o, predicates);
	}

	@Asynchronous
	@Override
	public final void toAll(final Object o) {
		toSpecific(o, Objects::nonNull);
	}

	@Asynchronous
	@SafeVarargs
	@Override
	public synchronized final void toAllExcept(final Object o, final Predicate<Session>... predicates) {
		final List<Session> toSendTo = new ArrayList<>();
		synchronized (clientList) {
			clientList.sessionStream()
					.filter(user -> ! testAgainst(user, predicates))
					.forEach(toSendTo::add);
		}
		toSendTo.forEach(session -> session.send(o));
	}

	@Asynchronous
	@Override
	public final void toRegistered(final Object o) {
		toRegistered(o, Objects::nonNull);
	}

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

	@SafeVarargs
	private final boolean testAgainst(final Session session, final Predicate<Session>... predicates) {
		for (final Predicate<Session> predicate : predicates) {
			if (! predicate.test(session)) {
				return false;
			}
		}
		return true;
	}


}
