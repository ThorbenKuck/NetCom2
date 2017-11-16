package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;

import java.io.IOException;

public interface Connector<Factory, Return> {

	Return establishConnection(final Factory factory) throws IOException, StartFailedException;

	Return establishConnection(final Class key, final Factory factory) throws IOException, StartFailedException;

	void shutDown() throws IOException;

}
