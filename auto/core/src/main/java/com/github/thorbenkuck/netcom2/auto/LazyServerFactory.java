package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.network.server.ConnectorCore;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

public interface LazyServerFactory {

	LazyServerFactory use(ObjectRepository objectRepository);

	ServerStart at(int port);

	ServerStart asNIO(int port);

	ServerStart asTCP(int port);

	ServerStart asUDP(int port);

	ServerStart as(int port, ConnectorCore connectorCore);
}
