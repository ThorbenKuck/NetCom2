package com.github.thorbenkuck.netcom2.network.server;

interface InternalDistributor extends Distributor {
	static InternalDistributor create(final ClientList clientList,
									  final DistributorRegistration distributorRegistration) {
		return new DistributorImpl(clientList, distributorRegistration);
	}

	DistributorRegistration getDistributorRegistration();
}
