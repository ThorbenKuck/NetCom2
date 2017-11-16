package com.github.thorbenkuck.netcom2.network.shared;

import java.util.Properties;

public interface SessionUpdater {

	SessionUpdater updateIdentified(final boolean to);

	SessionUpdater updateProperties(final Properties properties);

	SessionUpdater updateIdentifier(final String identifier);

	void sendOverNetwork();

}
