package de.thorbenkuck.netcom2.network.shared;

import de.thorbenkuck.netcom2.interfaces.SendBridge;
import de.thorbenkuck.netcom2.network.shared.heartbeat.HeartBeat;

import java.util.*;

public class SessionImpl implements Session {

	private final SendBridge sendBridge;
	private final Map<Class<?>, Pipeline<?>> pipelines = new HashMap<>();
	private final List<HeartBeat<Session>> heartBeats = new ArrayList<>();
	private volatile boolean identified = false;
	private volatile String identifier = "";
	private volatile Properties properties = new Properties();

	SessionImpl(SendBridge sendBridge) {
		this.sendBridge = sendBridge;
	}

	@Override
	public String toString() {
		return "Session{" +
				"identified=" + identified +
				", identifier='" + identifier + '\'' +
				", properties=" + properties +
				'}';
	}

	@Override
	public boolean isIdentified() {
		return identified;
	}

	@Override
	public void setIdentified(boolean identified) {
		this.identified = identified;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}

	@Override
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	@Override
	public void send(Object o) {
		sendBridge.send(o);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public <T> Pipeline<T> eventOf(Class<T> clazz) {
		pipelines.computeIfAbsent(clazz, k -> new QueuedPipeline<>());
		return (Pipeline<T>) pipelines.get(clazz);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public <T> void triggerEvent(Class<T> clazz, T t) {
		Pipeline<T> pipeline = (Pipeline<T>) pipelines.get(clazz);
		if (pipeline != null) {
			pipeline.run(t);
		}
	}

	@Override
	public void addHeartBeat(HeartBeat<Session> heartBeat) {
		heartBeats.add(heartBeat);
		heartBeat.run(this);
	}

	@Override
	public void removeHeartBeat(HeartBeat<Session> heartBeat) {
		HeartBeat<Session> heartBeat1 = heartBeats.get(heartBeats.indexOf(heartBeat));
		if (heartBeat1 != null) {
			heartBeat1.stop();
		}
	}
}
