package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.shared.User;
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
	public synchronized final void toSpecific(Object o, Predicate<User>... predicates) {
		clientList.userStream()
				.filter(user -> testAgainst(user, predicates))
				.forEach(client -> client.send(o));
	}



	@Override
	public final void toAllIdentified(Object o) {
		toSpecific(o, User::isIdentified);
	}

	@SafeVarargs
	@Override
	public final void toAllIdentified(Object o, Predicate<User>... predicates) {
		predicates[predicates.length] = User::isIdentified;
		toSpecific(o, predicates);
	}

	@Override
	public final void toAll(Object o) {
		toSpecific(o, Objects::nonNull);
	}

	@Override
	public synchronized final void toAllExcept(Object o, Predicate<User>[] predicates) {
		clientList.userStream()
				.filter(user -> ! testAgainst(user, predicates))
				.forEach(client -> client.send(o));
	}

	@Override
	public final void toRegistered(Object o) {
		toRegistered(o, User::isIdentified);
	}

	@Override
	@SafeVarargs
	public final void toRegistered(Object o, Predicate<User>... predicates) {
		distributorRegistration.getRegistered(o.getClass()).stream()
				.filter(user -> testAgainst(user, predicates))
				.forEach(user -> {
			LoggingUtil.getLogging().trace("Sending cache-update of " + o.getClass() + " to " + user);
			user.send(new CachePush(o));
		});
	}

	private boolean testAgainst(User user, Predicate<User>[] predicates) {
		for (Predicate<User> predicate : predicates) {
			if (! predicate.test(user)) {
				return false;
			}
		}
		return true;
	}


}
