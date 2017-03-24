package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.network.shared.User;

import java.util.function.Predicate;

public interface Distributor {

	void toAll(Object o);

	void to(Object o, Predicate<User>... predicates);

	void toRegistered(Object o);

}
