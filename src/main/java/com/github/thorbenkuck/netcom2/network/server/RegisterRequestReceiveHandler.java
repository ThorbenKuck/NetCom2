package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.CachePush;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RegisterResponse;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

@APILevel
class RegisterRequestReceiveHandler implements OnReceive<RegisterRequest> {

	private final Logging logging = Logging.unified();
	private DistributorRegistration distributorRegistration;
	private com.github.thorbenkuck.netcom2.network.shared.cache.Cache cache;

	@APILevel
	RegisterRequestReceiveHandler(final DistributorRegistration distributorRegistration, final Cache cache) {
		this.distributorRegistration = distributorRegistration;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public void accept(final Session session, final RegisterRequest o) {
		NetCom2Utils.parameterNotNull(session, o);
		logging.debug("Trying to register " + session + " to " + o.getCorrespondingClass());
		final Class<?> clazz = o.getCorrespondingClass();
		distributorRegistration.addRegistration(clazz, session);
		session.send(new RegisterResponse(o, true));
		cache.get(clazz).ifPresent(object -> session.send(new CachePush(object)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "RegisterRequestReceiveHandler{Handling internal client-registrations}";
	}
}
