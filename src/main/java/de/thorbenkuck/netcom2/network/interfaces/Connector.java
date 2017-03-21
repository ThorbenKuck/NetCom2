package de.thorbenkuck.netcom2.network.interfaces;

import java.io.IOException;

public interface Connector<Factory> {

	void establishConnection(Factory factory) throws IOException;

	void disconnect() throws IOException;

}
