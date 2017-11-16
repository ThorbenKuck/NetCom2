package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;

class SessionUpdateHandler implements OnReceive<Session> {
	@Override
	public void accept(final Session session, final Session newSession) {
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
