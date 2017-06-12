package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.comm.model.CachePush;

import java.util.Objects;
import java.util.function.Predicate;

class DistributorImpl implements InternalDistributor {

	private ClientList clientList;
	private DistributorRegistration distributorRegistration;

	DistributorImpl(ClientList clientList, DistributorRegistration distributorRegistration) {
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

	@Override
	@SafeVarargs
	public synchronized final void toSpecific(Object o, Predicate<Session>... predicates) {
		clientList.userStream()
				.filter(user -> testAgainst(user, predicates))
				.forEach(client -> client.send(o));
	}


	@Override
	public final void toAllIdentified(Object o) {
		toSpecific(o, Session::isIdentified);
	}

	@SafeVarargs
	@Override
	public final void toAllIdentified(Object o, Predicate<Session>... predicates) {
		predicates[predicates.length] = Session::isIdentified;
		toSpecific(o, predicates);
	}

	@Override
	public final void toAll(Object o) {
		toSpecific(o, Objects::nonNull);
	}

	@Override
	public synchronized final void toAllExcept(Object o, Predicate<Session>[] predicates) {
		clientList.userStream()
				.filter(user -> ! testAgainst(user, predicates))
				.forEach(client -> client.send(o));
	}

	@Override
	public final void toRegistered(Object o) {
		toRegistered(o, Session::isIdentified);
	}

	@Override
	@SafeVarargs
	public final void toRegistered(Object o, Predicate<Session>... predicates) {
		distributorRegistration.getRegistered(o.getClass()).stream()
				.filter(user -> testAgainst(user, predicates))
				.forEach(user -> {
					NetComLogging.getLogging().trace("Sending cache-update at " + o.getClass() + " to " + user);
					user.send(new CachePush(o));
				});
	}

	private boolean testAgainst(Session session, Predicate<Session>[] predicates) {
		for (Predicate<Session> predicate : predicates) {
			if (! predicate.test(session)) {
				return false;
			}
		}
		return true;
	}


}
