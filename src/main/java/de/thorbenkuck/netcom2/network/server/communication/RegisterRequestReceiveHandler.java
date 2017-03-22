package de.thorbenkuck.netcom2.network.server.communication;

import de.thorbenkuck.netcom2.network.server.DistributorRegistration;
import de.thorbenkuck.netcom2.network.shared.User;
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
	public void run(User user, RegisterRequest o) {
		Class<?> clazz = o.getCorrespondingClass();
		distributorRegistration.addRegistration(clazz, user);
		user.send(new RegisterResponse(o, true));
		Cache.get(clazz).ifPresent(object -> user.send(new CachePush(object)));
	}

	@Override
	public String toString() {
		return "RegisterRequestReceiveHandler{Handling internal client-registrations}";
	}
}
