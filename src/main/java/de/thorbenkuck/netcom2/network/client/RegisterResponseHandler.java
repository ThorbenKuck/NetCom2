package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterResponse;

class RegisterResponseHandler implements OnReceive<RegisterResponse> {

	private final Logging logging = Logging.unified();
	private final Cache cache;
	private final InternalSender sender;

	RegisterResponseHandler(Cache cache, InternalSender sender) {
		this.cache = cache;
		this.sender = sender;
	}

	@Override
	public void accept(Session session, RegisterResponse o) {
		if (o.isOkay()) {
			cache.addCacheObserver(sender.removePendingObserver(o.getRequest().getCorrespondingClass()));
			logging.debug("Registered to Server-Push at " + o.getRequest().getCorrespondingClass());
		}
	}
}
