package de.thorbenkuck.netcom2.network.server.communication;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.server.DistributorRegistration;
import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.model.CachePush;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterResponse;

public class RegisterRequestReceiveHandler implements OnReceive<RegisterRequest> {

	private DistributorRegistration distributorRegistration;
	private Logging logging = LoggingUtil.getLogging();
	private Cache cache;

	public RegisterRequestReceiveHandler(DistributorRegistration distributorRegistration, Cache cache) {
		this.distributorRegistration = distributorRegistration;
		this.cache = cache;
	}

	@Override
	public void run(User user, RegisterRequest o) {
		Class<?> clazz = o.getCorrespondingClass();
		distributorRegistration.addRegistration(clazz, user);
		user.send(new RegisterResponse(o, true));
		cache.get(clazz).ifPresent(object -> {
			user.send(new CachePush(object));
		});
	}

	@Override
	public String toString() {
		return "RegisterRequestReceiveHandler{Handling internal Registrations}";
	}
}
