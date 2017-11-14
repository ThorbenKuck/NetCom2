package com.github.thorbenkuck.netcom2.network.shared;

import java.util.Properties;

public interface SessionUpdater {

	SessionUpdater updateIdentified(boolean to);

	SessionUpdater updateProperties(Properties properties);

	SessionUpdater updateIdentifier(String identifier);

	void sendOverNetwork();

}
