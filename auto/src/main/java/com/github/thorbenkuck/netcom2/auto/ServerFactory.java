package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.network.server.ConnectorCore;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

public interface ServerFactory {

	ServerFactory use(ObjectRepository objectRepository);

	ServerFactoryFinalizer use(ServerStart serverStart);

	ServerFactoryFinalizer at(int port);

	ServerFactoryFinalizer asNIO(int port);

	ServerFactoryFinalizer asTCP(int port);

	ServerFactoryFinalizer asUDP(int port);

	ServerFactoryFinalizer as(int port, ConnectorCore connectorCore);

}
