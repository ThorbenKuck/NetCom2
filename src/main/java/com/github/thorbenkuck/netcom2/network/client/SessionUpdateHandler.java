package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.SessionUpdate;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

@APILevel
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
}
