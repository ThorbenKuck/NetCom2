package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.network.shared.Session;

import java.util.function.Predicate;

public interface Distributor {

	void toSpecific(Object o, Predicate<Session>... predicates);

	void toAll(Object o);

	void toAllExcept(Object o, Predicate<Session>... predicates);

	void toAllIdentified(Object o);

	void toAllIdentified(Object o, Predicate<Session>... predicates);

	void toRegistered(Object o);

	void toRegistered(Object o, Predicate<Session>... predicates);
}
