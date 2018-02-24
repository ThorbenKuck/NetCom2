package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.cache.AbstractCacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.CachePush;
import de.thorbenkuck.keller.datatypes.interfaces.GenericObserver;

/**
 * This Handler will handle an received CachePush at the Client-Side.
 * <p>
 * It takes the received Object and puts it into the locally held cache. Further the mutex of the cache is used to ensure
 * Thread-Safety
 */
@APILevel
class CachePushHandler implements OnReceiveSingle<CachePush> {

	private final Logging logging = Logging.unified();
	private final Cache cache;

	CachePushHandler(final Cache cache) {
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public void accept(final CachePush cachePush) {
		logging.debug("Updating cache, based on received information!");
		try {
			cache.acquire();
			cache.addAndOverride(cachePush.getObject());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			cache.release();
		}
	}
}
