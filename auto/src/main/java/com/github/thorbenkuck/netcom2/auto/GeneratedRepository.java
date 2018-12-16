package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

class GeneratedRepository {

	private final List<OnReceiveWrapper> onReceiveWrapperCache = new ArrayList<>();
	private final List<ClientConnectedWrapper> clientConnectedWrappersCache = new ArrayList<>();

	final synchronized void read() {
		ServiceLoader<OnReceiveWrapper> onReceiveWrappers = ServiceLoader.load(OnReceiveWrapper.class);
		synchronized (onReceiveWrapperCache) {
			onReceiveWrappers.forEach(onReceiveWrapperCache::add);
		}
		ServiceLoader<ClientConnectedWrapper> clientConnectedWrappers = ServiceLoader.load(ClientConnectedWrapper.class);
		synchronized (clientConnectedWrappersCache) {
			clientConnectedWrappers.forEach(clientConnectedWrappersCache::add);
		}
	}

	final List<OnReceiveWrapper> getOnReceiveWrapper() {
		synchronized (onReceiveWrapperCache) {
			return new ArrayList<>(onReceiveWrapperCache);
		}
	}

	final void apply(ServerStart serverStart, ObjectRepository repository) {
		synchronized (onReceiveWrapperCache) {
			onReceiveWrapperCache.forEach(wrapper -> wrapper.apply(serverStart, repository));
		}

		synchronized (clientConnectedWrappersCache) {
			clientConnectedWrappersCache.forEach(wrapper -> wrapper.apply(serverStart, repository));
		}
	}

	final void apply(ClientStart clientStart, ObjectRepository repository) {
		synchronized (onReceiveWrapperCache) {
			onReceiveWrapperCache.forEach(wrapper -> wrapper.apply(clientStart, repository));
		}
	}
}
