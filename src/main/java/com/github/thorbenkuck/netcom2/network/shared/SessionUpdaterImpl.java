package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.SessionUpdate;

import java.util.Properties;

class SessionUpdaterImpl implements SessionUpdater {

	private final Session session;
	private final Logging logging = Logging.unified();

	SessionUpdaterImpl(Session session) {
		this.session = session;
	}

	@Override
	public SessionUpdater updateIdentified(boolean to) {
		try {
			session.acquire();
			session.setIdentified(to);
		} catch (InterruptedException e) {
			logging.catching(e);
		} finally {
			session.release();
		}
		return this;
	}

	@Override
	public SessionUpdater updateProperties(Properties properties) {
		try {
			session.acquire();
			session.setProperties(properties);
		} catch (InterruptedException e) {
			logging.catching(e);
		} finally {
			session.release();
		}
		return this;
	}

	@Override
	public SessionUpdater updateIdentifier(String identifier) {
		try {
			session.acquire();
			session.setIdentifier(identifier);
		} catch (InterruptedException e) {
			logging.catching(e);
		} finally {
			session.release();
		}
		return this;
	}

	@Override
	public void sendOverNetwork() {
		Session toSend = new SessionImpl(null);
		toSend.update()
				.updateProperties(new Properties(session.getProperties()))
				.updateIdentifier(session.getIdentifier())
				.updateIdentified(session.isIdentified());
		session.send(new SessionUpdate(toSend));
	}
}
