package de.thorbenkuck.netcom2.network.shared;

import de.thorbenkuck.netcom2.interfaces.SendBridge;

import java.util.Properties;

public class UserImpl implements User {

	private final SendBridge sendBridge;
	private volatile boolean identified = false;
	private volatile String identifier = "";
	private volatile Properties properties = new Properties();

	public UserImpl(SendBridge sendBridge) {
		this.sendBridge = sendBridge;
	}

	@Override
	public String toString() {
		return "UserImpl{" +
				"identified=" + identified +
				", identifier='" + identifier + '\'' +
				", properties=" + properties +
				'}';
	}

	public boolean isIdentified() {
		return identified;
	}

	public void setIdentified(boolean identified) {
		this.identified = identified;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void send(Object o) {
		sendBridge.send(o);
	}
}
