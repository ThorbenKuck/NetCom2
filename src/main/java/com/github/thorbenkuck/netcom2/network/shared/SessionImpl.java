package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.interfaces.SendBridge;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.heartbeat.HeartBeat;
import com.github.thorbenkuck.netcom2.network.synchronization.DefaultSynchronize;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * {@inheritDoc}
 * This Class is not public by Design. It is package-private, so that it might change its behaviour, while still conforming
 * to its interface-standards. This means, it might change its signature in the future.
 * <p>
 * Note that only the Methods: {@link #triggerPrimation()}, {@link #primed()} and {@link #newPrimation()} are marked final,
 * so that the default behaviour of the internal Mechanisms is ensured.
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.shared.SessionImplTest")
class SessionImpl implements Session {

	private static final long serialVersionUID = 4414647424220391756L;
	private transient final SendBridge sendBridge;
	private transient final Map<Class<?>, Pipeline<?>> pipelines = new HashMap<>();
	private transient final List<HeartBeat<Session>> heartBeats = new ArrayList<>();
	private transient final UUID uuid;
	private transient final Logging logging = Logging.unified();
	private transient final Synchronize synchronize = new DefaultSynchronize();
	private transient final Semaphore semaphore = new Semaphore(1);
	private transient SessionUpdater sessionUpdater;
	private volatile boolean identified = false;
	private volatile String identifier = "";
	private volatile Properties properties = new Properties();

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
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof SessionImpl)) {
			return false;
		}

		SessionImpl that = (SessionImpl) obj;

		return uuid.equals(that.uuid);
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
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
	 *
	 * @throws IllegalArgumentException if the identifier is null
	 */
	@Override
	public void setIdentifier(final String identifier) {
		NetCom2Utils.parameterNotNull(identifier);
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
	 *
	 * @throws IllegalArgumentException if the properties is null
	 */
	@Override
	public void setProperties(final Properties properties) {
		NetCom2Utils.parameterNotNull(properties);
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the object is null
	 */
	@Override
	public void send(final Object o) {
		NetCom2Utils.parameterNotNull(o);
		sendBridge.send(o);
	}

	/**
	 * {@inheritDoc}
	 *
	 * The SuppressWarnings tag is used because of the type erasure of the generic type T. We ensure this at runtime.
	 *
	 * @throws IllegalArgumentException if the class is null
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> Pipeline<T> eventOf(final Class<T> clazz) {
		NetCom2Utils.parameterNotNull(clazz);
		pipelines.computeIfAbsent(clazz, k -> {
			logging.trace("Adding new SessionEventPipeline for " + clazz);
			return Pipeline.unifiedCreation();
		});
		return (Pipeline<T>) pipelines.get(clazz);
	}

	/**
	 * {@inheritDoc}
	 *
	 * The SuppressWarnings tag is used because of the type erasure of the generic type T
	 *
	 * @throws IllegalArgumentException if the class or the object is null
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> void triggerEvent(final Class<T> clazz, T t) {
		NetCom2Utils.parameterNotNull(clazz, t);
		final Pipeline<T> pipeline = (Pipeline<T>) pipelines.get(clazz);
		if (pipeline != null) {
			pipeline.run(t);
		} else {
			throw new IllegalArgumentException("No Event set for " + clazz);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the HeartBeat is null
	 */
	@Override
	public void addHeartBeat(final HeartBeat<Session> heartBeat) {
		NetCom2Utils.parameterNotNull(heartBeat);
		heartBeats.add(heartBeat);
		heartBeat.parallel().run(this);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the HeartBeat is null
	 */
	@Override
	public void removeHeartBeat(final HeartBeat<Session> heartBeat) {
		NetCom2Utils.parameterNotNull(heartBeat);
		if (!heartBeats.contains(heartBeat)) {
			logging.warn("The HeartBeat " + heartBeat + " was never set.");
			return;
		}
		heartBeats.remove(heartBeat);
		heartBeat.stop();
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
