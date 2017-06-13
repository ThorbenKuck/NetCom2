package de.thorbenkuck.netcom2.network.interfaces;

import de.thorbenkuck.netcom2.exceptions.StartFailedException;

import java.io.IOException;

public interface Connector<Factory, Return> {

	Return establishConnection(Factory factory) throws IOException, StartFailedException;

	Return establishConnection(Class key, Factory factory) throws IOException, StartFailedException;

	void shutDown() throws IOException;

}
