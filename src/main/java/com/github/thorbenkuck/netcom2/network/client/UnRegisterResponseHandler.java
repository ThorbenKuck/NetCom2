package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.logging.TraceLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterResponse;

@APILevel
class UnRegisterResponseHandler implements OnReceiveSingle<UnRegisterResponse> {

	private final Logging logging = Logging.unified();
	private final Cache cache;
	private final InternalSender sender;

	@APILevel
	UnRegisterResponseHandler(final Cache cache, final InternalSender sender) {
		this.cache = cache;
		this.sender = sender;
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public void accept(final UnRegisterResponse o) {
		if (o.isOkay()) {
			try {
				cache.acquire();
				cache.addCacheObserver(sender.removePendingObserver(o.getRequest().getCorrespondingClass()));
				logging.debug("Unregistered to Server-Push at " + o.getRequest().getCorrespondingClass());
			} catch (InterruptedException e) {
				logging.catching(e);
			} finally {
				cache.release();
			}
		}
	}
}
