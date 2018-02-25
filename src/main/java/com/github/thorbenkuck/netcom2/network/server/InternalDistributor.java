package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

@APILevel
interface InternalDistributor extends Distributor {

	/**
	 * Creates an new internal distributor.
	 *
	 * This is used, to hide the implementation of even the InternalDistributor.
	 *
	 * @param clientList the ClientList, this works on
	 * @param distributorRegistration the Registration, that is used for the cache
	 * @return a new InternalDistributor instantiation
	 */
	static InternalDistributor create(final ClientList clientList,
									  final DistributorRegistration distributorRegistration) {
		return new DistributorImpl(clientList, distributorRegistration);
	}

	/**
	 * This Method returns an internally maintained DistributorRegistration, used for the {@link com.github.thorbenkuck.netcom2.network.shared.cache.Cache}.
	 *
	 * This is only accessible internally, so that no fiddling with the DistributorRegistration is done, because that may
	 * in the best case hold up the process, while in the worst case break the process.
	 *
	 * @return the internally maintained DistributorRegistration
	 */
	@APILevel
	DistributorRegistration getDistributorRegistration();
}
