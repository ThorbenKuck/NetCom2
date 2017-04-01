package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.network.shared.User;

import java.util.function.Predicate;

public interface Distributor {

	void toSpecific(Object o, Predicate<User>... predicates);

	void toAll(Object o);

	void toAllExcept(Object o, Predicate<User>... predicates);

	void toAllIdentified(Object o);

	void toAllIdentified(Object o, Predicate<User>... predicates);

	void toRegistered(Object o);

	void toRegistered(Object o, Predicate<User>... predicates);
}
