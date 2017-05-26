package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OnReceiveHandler {

	private final ExecutorService threadPool = Executors.newCachedThreadPool();

	public <T> void handleRegistration(OnReceive<T> onReceive) {
		threadPool.submit(onReceive::onRegistration);
	}

	public <T> void handleUnRegistration(OnReceive<T> onReceive) {
		threadPool.submit(onReceive::onUnRegistration);
	}
}
