package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.util.function.Predicate;

public interface Distributor {

	void toSpecific(final Object o, final Predicate<Session>... predicates);

	void toAll(final Object o);

	void toAllExcept(final Object o, final Predicate<Session>... predicates);

	void toAllIdentified(final Object o);

	void toAllIdentified(final Object o, final Predicate<Session>... predicates);

	void toRegistered(final Object o);

	void toRegistered(final Object o, final Predicate<Session>... predicates);
}
