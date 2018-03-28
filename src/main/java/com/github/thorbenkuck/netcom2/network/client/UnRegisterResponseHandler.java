package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterResponse;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This Class handles {@link UnRegisterResponse}s, received over the Network.
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.client.UnRegisterResponseHandlerTest")
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
		NetCom2Utils.parameterNotNull(o);
		if (o.isOkay()) {
			try {
				cache.acquire();
				cache.removeCacheObserver(sender.removePendingObserver(o.getRequest().getCorrespondingClass()));
				logging.debug("Unregistered to Server-Push at " + o.getRequest().getCorrespondingClass());
			} catch (InterruptedException e) {
				logging.catching(e);
			} finally {
				cache.release();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "UnRegisterResponseHandler{" +
				"logging=" + logging +
				", cache=" + cache +
				", sender=" + sender +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof UnRegisterResponseHandler)) return false;

		UnRegisterResponseHandler handler = (UnRegisterResponseHandler) o;

		return logging.equals(handler.logging) && cache.equals(handler.cache) && sender.equals(handler.sender);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = logging.hashCode();
		result = 31 * result + cache.hashCode();
		result = 31 * result + sender.hashCode();
		return result;
	}
}
