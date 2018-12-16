package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.network.client.ClientCore;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;

public interface ClientFactory {

	ClientFactory use(ObjectRepository objectRepository);

	ClientFactoryFinalizer use(ClientStart clientStart);

	ClientFactoryFinalizer at(String address, int port);

	ClientFactoryFinalizer asNIO(String address, int port);

	ClientFactoryFinalizer asTCP(String address, int port);

	ClientFactoryFinalizer asUDP(String address, int port);

	ClientFactoryFinalizer as(String address, int port, ClientCore clientCore);
}
