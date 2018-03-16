package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.interfaces.SendBridge;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.heartbeat.HeartBeat;
import com.github.thorbenkuck.netcom2.network.synchronization.DefaultSynchronize;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * {@inheritDoc}
 * This Class is not public by Design. It is package-private, so that it might change its behaviour, while still conforming
 * to its interface-standards. This means, it might change its signature in the future.
 * <p>
 * Note that only the Methods: {@link #triggerPrimation()}, {@link #primed()} and {@link #newPrimation()} are marked final,
 * so that the default behaviour of the internal Mechanisms is ensured.
 */
@APILevel
class SessionImpl implements Session {

	private static final long serialVersionUID = 4414647424220391756L;
	private final SendBridge sendBridge;
	private final Map<Class<?>, Pipeline<?>> pipelines = new HashMap<>();
	private final List<HeartBeat<Session>> heartBeats = new ArrayList<>();
	private final UUID uuid;
	private final Logging logging = Logging.unified();
	private final Synchronize synchronize = new DefaultSynchronize();
	private final Semaphore semaphore = new Semaphore(1);
	private volatile boolean identified = false;
	private volatile String identifier = "";
	private volatile Properties properties = new Properties();
	private SessionUpdater sessionUpdater;

	@APILevel
	SessionImpl(final SendBridge sendBridge) {
		this.sendBridge = sendBridge;
		this.uuid = UUID.randomUUID();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj != null && obj.getClass().equals(SessionImpl.class) && ((SessionImpl) obj).uuid.equals(uuid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Session{" +
				"identified=" + identified +
				", identifier='" + identifier + '\'' +
				", properties=" + properties +
				", id=" + uuid +
				", events=" + pipelines.keySet() +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isIdentified() {
		return identified;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentified(final boolean identified) {
		this.identified = identified;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Properties getProperties() {
		return properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperties(final Properties properties) {
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(final Object o) {
		sendBridge.send(o);
	}

	/**
	 * {@inheritDoc}
	 * The SuppressWarnings tag is used because of the type erasure of the generic type T
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public <T> Pipeline<T> eventOf(final Class<T> clazz) {
		pipelines.computeIfAbsent(clazz, k -> {
			logging.trace("Adding new SessionEventPipeline for " + clazz);
			return Pipeline.unifiedCreation();
		});
		return (Pipeline<T>) pipelines.get(clazz);
	}

	/**
	 * {@inheritDoc}
	 * The SuppressWarnings tag is used because of the type erasure of the generic type T
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public <T> void triggerEvent(final Class<T> clazz, T t) {
		final Pipeline<T> pipeline = (Pipeline<T>) pipelines.get(clazz);
		if (pipeline != null) {
			pipeline.run(t);
		} else {
			throw new IllegalArgumentException("No Event set for " + clazz);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addHeartBeat(final HeartBeat<Session> heartBeat) {
		heartBeats.add(heartBeat);
		heartBeat.parallel().run(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeHeartBeat(final HeartBeat<Session> heartBeat) {
		final HeartBeat<Session> heartBeat1 = heartBeats.remove(heartBeats.indexOf(heartBeat));
		if (heartBeat1 != null) {
			heartBeat1.stop();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void triggerPrimation() {
		synchronized (synchronize) {
			synchronize.goOn();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Awaiting primed() {
		return synchronize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void newPrimation() {
		try {
			primed().synchronize();
			synchronized (synchronize) {
				synchronize.reset();
			}
		} catch (final InterruptedException e) {
			logging.catching(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SessionUpdater update() {
		try {
			acquire();
			if (sessionUpdater == null) {
				sessionUpdater = new SessionUpdaterImpl(this);
			}
		} catch (InterruptedException e) {
			logging.catching(e);
		} finally {
			release();
		}
		return sessionUpdater;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void acquire() throws InterruptedException {
		semaphore.acquire();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		semaphore.release();
	}
}
