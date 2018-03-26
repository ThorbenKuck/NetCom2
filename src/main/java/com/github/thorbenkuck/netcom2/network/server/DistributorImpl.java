package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.CachePush;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This implementation of the InternalDistributor, is maintained manually within the {@link ServerStart}.
 *
 * @version 1.0
 * @since 1.0
 */
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
	private boolean testAgainst(final Session session, final List<Predicate<Session>> predicates) {
		for (final Predicate<Session> predicate : predicates) {
			if (!predicate.test(session)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Filters the client list by the given predicates and returns the resulting filtered list.
	 * An element is <b>not</b> filtered out, if it matches all predicates.
	 *
	 * @param predicates The predicates to filter by
	 * @return The filtered list
	 */
	private List<Session> filter(final List<Predicate<Session>> predicates) {
		synchronized (clientList) {
			return clientList.sessionStream().filter(s -> testAgainst(s, predicates)).collect(Collectors.toList());
		}
	}

	/**
	 * Filters the given sessions list by the given predicates. A session is <b>not</b> filtered out,
	 * if it matches all predicates.
	 *
	 * @param sessions   The sessions to be filtered
	 * @param predicates The predicates to filter by
	 * @return The filtered list
	 */
	private List<Session> filter(List<Session> sessions, final List<Predicate<Session>> predicates) {
		return sessions.stream().filter(s -> testAgainst(s, predicates)).collect(Collectors.toList());
	}

	/**
	 * Sends the specified object to all of the given sessions.
	 *
	 * @param sessions The sessions to send the specified object to.
	 * @param o        The object to be sent
	 */
	private void send(final List<Session> sessions, final Object o) {
		NetCom2Utils.parameterNotNull(sessions, o);
		send(sessions, () -> o);
	}

	/**
	 * Sends objects received from the specified supplier to all of the given sessions.
	 *
	 * @param sessions The sessions to send to
	 * @param supplier The supplier to get objects from
	 */
	private void send(final List<Session> sessions, Supplier<Object> supplier) {
		sessions.forEach(s -> s.send(supplier.get()));
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
	@Override
	public void toSpecific(final Object o, final List<Predicate<Session>> predicates) {
		NetCom2Utils.parameterNotNull(o, predicates);
		final List<Session> clientsToSendTo = filter(predicates);
		send(clientsToSendTo, o);
	}

	/**
	 * {@inheritDoc}
	 */
	@SafeVarargs
	@Asynchronous
	@Override
	public synchronized final void toSpecific(final Object o, final Predicate<Session>... predicates) {
		toSpecific(o, Arrays.asList(predicates));
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public final void toAll(final Object o) {
		toSpecific(o, Collections.emptyList());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized final void toAllExcept(final Object o, final List<Predicate<Session>> predicates) {
		NetCom2Utils.parameterNotNull(o, predicates);

		final List<Session> sessions = new ArrayList<>();
		synchronized (clientList) {
			clientList.sessionStream().forEach(sessions::add);
		}
		predicates.forEach(sessions::removeIf);
		send(sessions, o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@SafeVarargs
	@Override
	public synchronized final void toAllExcept(final Object o, final Predicate<Session>... predicates) {
		toAllExcept(o, Arrays.asList(predicates));
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public final void toAllIdentified(final Object o) {
		toSpecific(o, Collections.singletonList(Session::isIdentified));
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@SafeVarargs
	@Override
	public final void toAllIdentified(final Object o, final Predicate<Session>... predicates) {
		List<Predicate<Session>> list = new ArrayList<>();
		list.addAll(Arrays.asList(predicates));
		list.add(Session::isIdentified);
		toSpecific(o, list);
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public final void toRegistered(final Object o) {
		toRegistered(o, Collections.emptyList());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void toRegistered(final Object o, final List<Predicate<Session>> predicates) {
		NetCom2Utils.parameterNotNull(o, predicates);
		final List<Session> registeredUsers;
		synchronized (distributorRegistration) {
			registeredUsers = distributorRegistration.getRegistered(o.getClass());
		}
		logging.trace("Trying to send " + o + " to " + registeredUsers);

		List<Session> filteredToSendTo = filter(registeredUsers, predicates);
		send(filteredToSendTo, () -> new CachePush(o));
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@SafeVarargs
	@Override
	public final void toRegistered(final Object o, final Predicate<Session>... predicates) {
		toRegistered(o, Arrays.asList(predicates));
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
