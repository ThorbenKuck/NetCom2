package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterResponse;

class UnRegisterResponseHandler implements OnReceive<UnRegisterResponse> {

	private final Logging logging = Logging.unified();
	private final Cache cache;
	private final InternalSender sender;

	UnRegisterResponseHandler(Cache cache, InternalSender sender) {
		this.cache = cache;
		this.sender = sender;
	}

	@Asynchronous
	@Override
	public void accept(Session session, UnRegisterResponse o) {
		if (o.isOkay()) {
			cache.addCacheObserver(sender.removePendingObserver(o.getRequest().getCorrespondingClass()));
			logging.debug("Unregistered to Server-Push at " + o.getRequest().getCorrespondingClass());
		}
	}
}