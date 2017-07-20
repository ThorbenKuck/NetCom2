package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.CachePush;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RegisterResponse;

class RegisterRequestReceiveHandler implements OnReceive<RegisterRequest> {

	private final Logging logging = Logging.unified();
	private DistributorRegistration distributorRegistration;
	private com.github.thorbenkuck.netcom2.network.shared.cache.Cache Cache;

	RegisterRequestReceiveHandler(DistributorRegistration distributorRegistration, Cache Cache) {
		this.distributorRegistration = distributorRegistration;
		this.Cache = Cache;
	}

	@Asynchronous
	@Override
	public void accept(Session session, RegisterRequest o) {
		logging.debug("Trying to register " + session + " to " + o.getCorrespondingClass());
		Class<?> clazz = o.getCorrespondingClass();
		distributorRegistration.addRegistration(clazz, session);
		session.send(new RegisterResponse(o, true));
		Cache.get(clazz).ifPresent(object -> session.send(new CachePush(object)));
	}

	@Override
	public String toString() {
		return "RegisterRequestReceiveHandler{Handling internal client-registrations}";
	}
}
