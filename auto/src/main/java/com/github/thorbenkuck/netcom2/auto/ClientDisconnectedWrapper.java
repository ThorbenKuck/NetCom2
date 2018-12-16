package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

public interface ClientDisconnectedWrapper {

	void apply(ServerStart serverStart, ObjectRepository repository);

	void apply(ClientStart clientStart, ObjectRepository repository);

}
