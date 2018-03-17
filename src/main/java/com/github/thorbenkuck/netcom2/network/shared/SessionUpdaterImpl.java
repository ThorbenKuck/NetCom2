package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.SessionUpdate;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.Properties;

@APILevel
class SessionUpdaterImpl implements SessionUpdater {

	private final Session session;
	private final Logging logging = Logging.unified();

	@APILevel
	SessionUpdaterImpl(final Session session) {
		NetCom2Utils.parameterNotNull(session);
		this.session = session;
	}

	@Override
	public SessionUpdater updateIdentified(final boolean to) {
		try {
			session.acquire();
			session.setIdentified(to);
		} catch (final InterruptedException e) {
			logging.catching(e);
		} finally {
			session.release();
		}
		return this;
	}

	@Override
	public SessionUpdater updateProperties(final Properties properties) {
		NetCom2Utils.parameterNotNull(properties);
		try {
			session.acquire();
			session.setProperties(properties);
		} catch (final InterruptedException e) {
			logging.catching(e);
		} finally {
			session.release();
		}
		return this;
	}

	@Override
	public SessionUpdater updateIdentifier(final String identifier) {
		NetCom2Utils.parameterNotNull(identifier);
		try {
			session.acquire();
			session.setIdentifier(identifier);
		} catch (final InterruptedException e) {
			logging.catching(e);
		} finally {
			session.release();
		}
		return this;
	}

	@Override
	public void sendOverNetwork() {
		final Session toSend = new SessionImpl(null);
		toSend.update()
				.updateProperties(new Properties(session.getProperties()))
				.updateIdentifier(session.getIdentifier())
				.updateIdentified(session.isIdentified());
		session.send(new SessionUpdate(toSend));
	}
}
