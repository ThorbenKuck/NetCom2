package de.thorbenkuck.netcom2.network.server.communication;

import de.thorbenkuck.netcom2.network.server.DistributorRegistration;
import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;
import de.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterResponse;

public class UnRegisterRequestReceive implements OnReceive<UnRegisterRequest> {

	private DistributorRegistration distributorRegistration;

	public UnRegisterRequestReceive(DistributorRegistration distributorRegistration) {
		this.distributorRegistration = distributorRegistration;
	}

	@Override
	public void run(User user, UnRegisterRequest o) {
		distributorRegistration.removeRegistration(o.getCorrespondingClass(), user);
		user.send(new UnRegisterResponse(o, true));
	}
}
