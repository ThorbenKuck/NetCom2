package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.interfaces.SendBridge;
import com.github.thorbenkuck.netcom2.network.client.DefaultSynchronize;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.heartbeat.HeartBeat;

import java.util.*;

/**
 * {@inheritDoc}
 * This Class is public, so it might be extended to create an custom Session.
 *
 * Note that only the Methods: {@link #triggerPrimation()}, {@link #primed()} and {@link #newPrimation()} are marked final,
 * so that the default behaviour of the internal Mechanisms is ensured.
 */
public class SessionImpl implements Session {

	private final SendBridge sendBridge;
	private final Map<Class<?>, Pipeline<?>> pipelines = new HashMap<>();
	private final List<HeartBeat<Session>> heartBeats = new ArrayList<>();
	private final UUID uuid;
	private final Logging logging = Logging.unified();
	private volatile boolean identified = false;
	private volatile String identifier = "";
	private volatile Properties properties = new Properties();
	private final Synchronize synchronize = new DefaultSynchronize();

	SessionImpl(SendBridge sendBridge) {
		this.sendBridge = sendBridge;
		this.uuid = UUID.randomUUID();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
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
	public void setIdentified(boolean identified) {
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
	public void setIdentifier(String identifier) {
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
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(Object o) {
		sendBridge.send(o);
	}

	/**
	 * {@inheritDoc}
	 * The SuppressWarnings tag is used because of the type erasure of the generic type T
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public <T> Pipeline<T> eventOf(Class<T> clazz) {
		pipelines.computeIfAbsent(clazz, k -> new QueuedPipeline<>());
		return (Pipeline<T>) pipelines.get(clazz);
	}

	/**
	 * {@inheritDoc}
	 * The SuppressWarnings tag is used because of the type erasure of the generic type T
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public <T> void triggerEvent(Class<T> clazz, T t) {
		Pipeline<T> pipeline = (Pipeline<T>) pipelines.get(clazz);
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
	public void addHeartBeat(HeartBeat<Session> heartBeat) {
		heartBeats.add(heartBeat);
		heartBeat.parallel().run(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeHeartBeat(HeartBeat<Session> heartBeat) {
		HeartBeat<Session> heartBeat1 = heartBeats.get(heartBeats.indexOf(heartBeat));
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
			synchronized(synchronize) {
				synchronize.reset();
			}
		} catch (InterruptedException e) {
			logging.catching(e);
		}
	}
}
