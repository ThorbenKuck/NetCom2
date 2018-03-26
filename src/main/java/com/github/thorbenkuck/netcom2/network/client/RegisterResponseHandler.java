package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RegisterResponse;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This Class handles an {@link RegisterResponse}, received from the ServerStart
 *
 * @version 1.0
 * @since 1.0
 */
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

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public void accept(final RegisterResponse o) {
		NetCom2Utils.parameterNotNull(o);
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

	@Override
	public String toString() {
		return "RegisterResponseHandler{" +
				"cache=" + cache +
				", sender=" + sender +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RegisterResponseHandler)) return false;

		RegisterResponseHandler handler = (RegisterResponseHandler) o;

		if (!logging.equals(handler.logging)) return false;
		if (!cache.equals(handler.cache)) return false;
		return sender.equals(handler.sender);
	}

	@Override
	public int hashCode() {
		int result = logging.hashCode();
		result = 31 * result + cache.hashCode();
		result = 31 * result + sender.hashCode();
		return result;
	}
}
