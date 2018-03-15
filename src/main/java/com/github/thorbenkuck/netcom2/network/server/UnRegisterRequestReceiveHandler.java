package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterResponse;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

@APILevel
class UnRegisterRequestReceiveHandler implements OnReceive<UnRegisterRequest> {

	private final Logging logging = Logging.unified();
	private final DistributorRegistration distributorRegistration;

	@APILevel
	UnRegisterRequestReceiveHandler(final DistributorRegistration distributorRegistration) {
		this.distributorRegistration = distributorRegistration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public void accept(final Session session, final UnRegisterRequest o) {
		NetCom2Utils.parameterNotNull(session, o);
		logging.debug("Trying to unregister session " + session + " from " + o.getCorrespondingClass());
		distributorRegistration.removeRegistration(o.getCorrespondingClass(), session);
		session.send(new UnRegisterResponse(o, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "UnRegisterRequestReceiveHandler{Handling internal client-registrations}";
	}
}
