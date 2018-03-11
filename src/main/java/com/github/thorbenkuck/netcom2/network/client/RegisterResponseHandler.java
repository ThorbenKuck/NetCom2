package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RegisterResponse;

@APILevel
class RegisterResponseHandler implements OnReceiveSingle<RegisterResponse> {

	private final Logging logging = Logging.unified();
	private final Cache cache;
	private final InternalSender sender;

	@APILevel
	RegisterResponseHandler(final Cache cache, final InternalSender sender) {
		this.cache = cache;
		this.sender = sender;
	}

	@Asynchronous
	@Override
	public void accept(final RegisterResponse o) {
		if (o.isOkay()) {
			try {
				cache.acquire();
				cache.addCacheObserver(sender.removePendingObserver(o.getRequest().getCorrespondingClass()));
				logging.debug("Registered to Server-Push at " + o.getRequest().getCorrespondingClass());
			} catch (InterruptedException e) {
				logging.catching(e);
			} finally {
				cache.release();
			}
		}
	}
}
