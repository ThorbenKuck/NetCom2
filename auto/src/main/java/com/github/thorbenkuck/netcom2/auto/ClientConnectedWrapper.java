package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.network.server.ServerStart;

public interface ClientConnectedWrapper {

	void apply(ServerStart serverStart, ObjectRepository objectRepository);

}
