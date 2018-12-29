package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

class GeneratedRepository {

	private final List<OnReceiveWrapper> onReceiveWrapperCache = new ArrayList<>();
	private final List<ClientConnectedWrapper> clientConnectedWrappersCache = new ArrayList<>();
	private final List<ClientDisconnectedWrapper> clientDisconnectedWrappersCache = new ArrayList<>();
	private final List<ServerPreConfiguration> serverPreConfigurationCache = new ArrayList<>();
	private final List<ClientPreConfiguration> clientPreConfigurationCache = new ArrayList<>();

	final synchronized void read() {
		// We do not want to limit parallel
		// work to much. This method is
		// synchronized, because we do not
		// want to have more than one thread
		// actually trying to read the
		// service files. The other
		// method however is not synchronized.
		// Therefor we have to synchronize
		// for every cache we have.
		ServiceLoader<OnReceiveWrapper> onReceiveWrappers = ServiceLoader.load(OnReceiveWrapper.class);
		synchronized (onReceiveWrapperCache) {
			onReceiveWrappers.forEach(onReceiveWrapperCache::add);
		}
		ServiceLoader<ClientConnectedWrapper> clientConnectedWrappers = ServiceLoader.load(ClientConnectedWrapper.class);
		synchronized (clientConnectedWrappersCache) {
			clientConnectedWrappers.forEach(clientConnectedWrappersCache::add);
		}
		ServiceLoader<ClientDisconnectedWrapper> clientDisconnectedWrappers = ServiceLoader.load(ClientDisconnectedWrapper.class);
		synchronized (clientDisconnectedWrappersCache) {
			clientDisconnectedWrappers.forEach(clientDisconnectedWrappersCache::add);
		}
		ServiceLoader<ServerPreConfiguration> serverPreConfigurationServiceLoader = ServiceLoader.load(ServerPreConfiguration.class);
		synchronized (serverPreConfigurationCache) {
			serverPreConfigurationServiceLoader.forEach(serverPreConfigurationCache::add);
		}
		ServiceLoader<ClientPreConfiguration> clientPreConfigurationServiceLoader = ServiceLoader.load(ClientPreConfiguration.class);
		synchronized (clientPreConfigurationCache) {
			clientPreConfigurationServiceLoader.forEach(clientPreConfigurationCache::add);
		}
	}

	final void apply(ServerStart serverStart, ObjectRepository repository) {
		synchronized (onReceiveWrapperCache) {
			onReceiveWrapperCache.forEach(wrapper -> wrapper.apply(serverStart, repository));
		}

		synchronized (clientConnectedWrappersCache) {
			clientConnectedWrappersCache.forEach(wrapper -> wrapper.apply(serverStart, repository));
		}
		synchronized (clientDisconnectedWrappersCache) {
			clientDisconnectedWrappersCache.forEach(wrapper -> wrapper.apply(serverStart, repository));
		}
		synchronized (serverPreConfigurationCache) {
			serverPreConfigurationCache.forEach(configuration -> configuration.accept(serverStart, repository));
		}
	}

	final void apply(ClientStart clientStart, ObjectRepository repository) {
		synchronized (onReceiveWrapperCache) {
			onReceiveWrapperCache.forEach(wrapper -> wrapper.apply(clientStart, repository));
		}
		synchronized (clientDisconnectedWrappersCache) {
			clientDisconnectedWrappersCache.forEach(wrapper -> wrapper.apply(clientStart, repository));
		}
		synchronized (clientPreConfigurationCache) {
			clientPreConfigurationCache.forEach(configuration -> configuration.accept(clientStart, repository));
		}
	}
}
