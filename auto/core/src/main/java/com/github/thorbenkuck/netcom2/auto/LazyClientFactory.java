package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.network.client.ClientCore;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;

public interface LazyClientFactory {

	LazyClientFactory use(ObjectRepository objectRepository);

	ClientStart at(String address, int port);

	ClientStart asNIO(String address, int port);

	ClientStart asTCP(String address, int port);

	ClientStart asUDP(String address, int port);

	ClientStart as(String address, int port, ClientCore clientCore);

}
