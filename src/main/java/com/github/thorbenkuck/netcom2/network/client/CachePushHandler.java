package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.CachePush;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This Handler will handle a received CachePush on the Client-Side.
 * <p>
 * It takes the received Object and puts it into the locally held cache. Further the mutex of the cache is used to ensure
 * Thread-Safety
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.client.CachePushHandlerTest")
class CachePushHandler implements OnReceiveSingle<CachePush> {

	private final Logging logging = Logging.unified();
	private final Cache cache;

	CachePushHandler(final Cache cache) {
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the cachePush is null
	 */
	@Asynchronous
	@Override
	public void accept(final CachePush cachePush) {
		NetCom2Utils.parameterNotNull(cachePush);
		logging.debug("Updating cache, based on received information!");
		try {
			cache.acquire();
			cache.addAndOverride(cachePush.getObject());
		} catch (InterruptedException e) {
			logging.catching(e);
		} finally {
			cache.release();
		}
	}
}
