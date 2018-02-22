package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.SessionUpdate;

@APILevel
class SessionUpdateHandler implements OnReceive<SessionUpdate> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(final Session session, final SessionUpdate sessionUpdate) {
		final Session newSession = sessionUpdate.getSession();
		try {
			session.acquire();
			session.update()
					.updateIdentified(newSession.isIdentified())
					.updateIdentifier(newSession.getIdentifier())
					.updateProperties(newSession.getProperties());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			session.release();
		}
	}
}
