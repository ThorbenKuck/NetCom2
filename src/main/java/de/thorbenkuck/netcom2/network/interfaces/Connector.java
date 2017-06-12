package de.thorbenkuck.netcom2.network.interfaces;

import java.io.IOException;

public interface Connector<Factory, Return> {

	Return establishConnection(Factory factory) throws IOException;

	Return establishConnection(Class key, Factory factory) throws IOException;

}
