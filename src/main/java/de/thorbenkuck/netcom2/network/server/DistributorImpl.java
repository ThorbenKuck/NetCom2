package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.comm.model.CachePush;

import java.util.function.Predicate;

public class DistributorImpl implements InternalDistributor {

	private ClientList clientList;
	private DistributorRegistration distributorRegistration;

	public DistributorImpl(ClientList clientList, DistributorRegistration distributorRegistration) {
		this.clientList = clientList;
		this.distributorRegistration = distributorRegistration;
	}

	@Override
	public void toAll(Object o) {
		to(o);
	}

	@Override
	@SafeVarargs
	public synchronized final void to(Object o, Predicate<User>... predicates) {
		clientList.userStream()
				.filter(User::isIdentified)
				.filter(user -> testAgainst(user, predicates))
				.forEach(client -> client.send(o));
	}

	@Override
	public void toRegistered(Object o) {
		distributorRegistration.getRegistered(o.getClass()).forEach(user -> {
			LoggingUtil.getLogging().trace("Sending cache-update of " + o.getClass() + " to " + user);
			user.send(new CachePush(o));
		});
	}

	@SafeVarargs
	private final boolean testAgainst(User user, Predicate<User>... predicates) {
		for (Predicate<User> predicate : predicates) {
			if (! predicate.test(user)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public DistributorRegistration getDistributorRegistration() {
		return distributorRegistration;
	}

	@Override
	public String toString() {
		return "Distributor{" +
				"clientList=" + clientList +
				'}';
	}
}
