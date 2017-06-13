package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;
import de.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterResponse;

class UnRegisterRequestReceiveHandler implements OnReceive<UnRegisterRequest> {

	private final Logging logging = Logging.unified();
	private DistributorRegistration distributorRegistration;

	UnRegisterRequestReceiveHandler(DistributorRegistration distributorRegistration) {
		this.distributorRegistration = distributorRegistration;
	}

	@Override
	public void accept(Session session, UnRegisterRequest o) {
		logging.debug("Trying to unregister session " + session + " from " + o.getCorrespondingClass());
		distributorRegistration.removeRegistration(o.getCorrespondingClass(), session);
		session.send(new UnRegisterResponse(o, true));
	}

	@Override
	public String toString() {
		return "UnRegisterRequestReceiveHandler{Handling internal client-registrations}";
	}
}
