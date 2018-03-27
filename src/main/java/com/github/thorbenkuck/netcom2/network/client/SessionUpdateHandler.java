package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.SessionUpdate;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This Class handles {@link SessionUpdate}, received over the Network.
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
class SessionUpdateHandler implements OnReceive<SessionUpdate> {

	private final Logging logging = Logging.unified();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(final Session session, final SessionUpdate sessionUpdate) {
		NetCom2Utils.parameterNotNull(session, sessionUpdate);
		final Session newSession = sessionUpdate.getSession();
		try {
			session.acquire();
			session.update()
					.updateIdentified(newSession.isIdentified())
					.updateIdentifier(newSession.getIdentifier())
					.updateProperties(newSession.getProperties());
		} catch (InterruptedException e) {
			logging.catching(e);
		} finally {
			session.release();
		}
	}

	@Override
	public String toString() {
		return "SessionUpdateHandler{" +
				"logging=" + logging +
				'}';
	}
}
