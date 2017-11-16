package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.CachePush;

public class CachePushHandler implements OnReceiveSingle<CachePush> {

	private final Logging logging = Logging.unified();
	private final Cache cache;

	public CachePushHandler(final Cache cache) {
		this.cache = cache;
	}

	@Asynchronous
	@Override
	public void accept(final CachePush o) {
		logging.debug("Updating cache, based on received information!");
		cache.addAndOverride(o.getObject());
	}
}
