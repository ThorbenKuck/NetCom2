package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.network.server.ServerStart;

import java.util.concurrent.ExecutorService;

public interface ServerFactoryFinalizer {

	void onCurrentThread();

	ServerStart on(ExecutorService executorService);

	ServerStart onNetComThreadPool();

	ServerStart onThread();

}
