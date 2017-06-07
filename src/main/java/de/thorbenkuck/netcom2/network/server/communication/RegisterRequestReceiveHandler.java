package de.thorbenkuck.netcom2.network.server.communication;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.server.DistributorRegistration;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.model.CachePush;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterResponse;

public class RegisterRequestReceiveHandler implements OnReceive<RegisterRequest> {

	private DistributorRegistration distributorRegistration;
	private Cache Cache;

	public RegisterRequestReceiveHandler(DistributorRegistration distributorRegistration, Cache Cache) {
		this.distributorRegistration = distributorRegistration;
		this.Cache = Cache;
	}

	@Override
	public void accept(Session session, RegisterRequest o) {
		LoggingUtil.getLogging().debug("Trying to register " + session + " to " + o.getCorrespondingClass());
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
