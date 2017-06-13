package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import de.thorbenkuck.netcom2.network.shared.comm.model.CachePush;

public class CachePushHandler implements OnReceiveSingle<CachePush> {

	private final Logging logging = Logging.unified();
	private final Cache cache;

	public CachePushHandler(Cache cache) {
		this.cache = cache;
	}

	@Override
	public void accept(CachePush o) {
		logging.debug("Updating cache, based on received information!");
		cache.addAndOverride(o.getObject());
	}
}
