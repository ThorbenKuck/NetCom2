package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.util.List;
import java.util.function.Predicate;

/**
 * This interface is used to allow internal components to to other things than what is exposed
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
interface InternalDistributor extends Distributor {

	/**
	 * Creates an new internal distributor.
	 * <p>
	 * This is used, to hide the implementation of even the InternalDistributor.
	 *
	 * @param clientList              the ClientList, this works on
	 * @param distributorRegistration the Registration, that is used for the cache
	 * @return a new InternalDistributor instantiation
	 */
	static InternalDistributor create(final ClientList clientList,
	                                  final DistributorRegistration distributorRegistration) {
		return new DistributorImpl(clientList, distributorRegistration);
	}

	/**
	 * This Method returns an internally maintained DistributorRegistration, used for the {@link com.github.thorbenkuck.netcom2.network.shared.cache.Cache}.
	 * <p>
	 * This is only accessible internally, so that no fiddling with the DistributorRegistration is done, because that may
	 * in the best case hold up the process, while in the worst case break the process.
	 *
	 * @return the internally maintained DistributorRegistration
	 */
	@APILevel
	DistributorRegistration getDistributorRegistration();

	/**
	 * Sends the Object o, to only one specific user, specified by the list of predicates
	 *
	 * @param o          the object to send
	 * @param predicates the predicate describing the receiver
	 */
	void toSpecific(Object o, List<Predicate<Session>> predicates);

	/**
	 * Sends the Object o, to all users, except those specified by the list of predicates
	 *
	 * @param o          the object to send
	 * @param predicates the predicate describing the excluded receiver
	 */
	void toAllExcept(Object o, List<Predicate<Session>> predicates);

	/**
	 * Sends the Object o, to specific users which registered to the type of the Object, further reduced by the list of predicates
	 *
	 * @param o          the object to send
	 * @param predicates the predicate describing the receiver
	 */
	void toRegistered(Object o, List<Predicate<Session>> predicates);
}
