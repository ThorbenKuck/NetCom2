package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.interfaces.Module;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.util.function.Predicate;

public interface Distributor extends Module<ServerStart> {

	static Distributor open(final ServerStart serverStart) {
		final NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);

		return distributor;
	}

	/**
	 * Sends the specified object to all clients satisfying <b>all</b> given predicates,
	 * by using their DefaultConnection.
	 *
	 * @param o          The object to send
	 * @param predicate The predicates to filter by
	 */
	void toSpecific(final Object o, final Predicate<Session> predicate);

	/**
	 * Sends the specified object to <b>all</b> clients, using their DefaultConnection.
	 *
	 * @param o The object to send
	 */
	void toAll(final Object o);

	/**
	 * Sends the specified object to all clients that do <b>not satisfy any</b> of the given predicates.
	 * The sending happens using the clients' DefaultConnection.
	 *
	 * @param o          The object to send
	 * @param predicate  The predicates to filter by
	 */
	void toAllExcept(final Object o, final Predicate<Session> predicate);

	/**
	 * Sends the given object to all clients that are identified.
	 * The clients' DefaultConnection will be used.
	 *
	 * @param o The object to send
	 */
	void toAllIdentified(final Object o);

	/**
	 * Sends the given object to all clients that are identified <b>and</b> satisfy all predicates.
	 * The clients' DefaultConnection will be used.
	 *
	 * @param o         The object to send
	 * @param predicate The predicate to filter by
	 */
	void toAllIdentified(final Object o, final Predicate<Session> predicate);

	/**
	 * Sends the given object to all clients that are registered.
	 * The clients' DefaultConnection will be used.
	 *
	 * @param o The object to send
	 */
	void toRegistered(final Object o);
}
