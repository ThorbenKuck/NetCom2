package de.thorbenkuck.netcom2.network.server;

interface InternalDistributor extends Distributor {
	static InternalDistributor create(ClientList clientList, DistributorRegistration distributorRegistration) {
		return new DistributorImpl(clientList, distributorRegistration);
	}

	DistributorRegistration getDistributorRegistration();
}
