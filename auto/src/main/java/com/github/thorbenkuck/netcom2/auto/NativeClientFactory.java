package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.network.client.ClientCore;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;

import java.net.InetSocketAddress;

class NativeClientFactory implements ClientFactory {

	private final GeneratedRepository repository;
	private final Value<ObjectRepository> objectRepositoryValue = Value.emptySynchronized();

	NativeClientFactory(GeneratedRepository repository) {
		this.repository = repository;
	}

	private void connect(ClientStart clientStart) {
		ObjectRepository objectRepository = objectRepositoryValue.get();
		repository.apply(clientStart, objectRepository);
	}

	@Override
	public ClientFactory use(ObjectRepository objectRepository) {
		objectRepositoryValue.set(objectRepository);
		return this;
	}

	@Override
	public ClientFactoryFinalizer use(ClientStart clientStart) {
		connect(clientStart);

		return new NativeClientFactoryFinalizer(clientStart);
	}

	@Override
	public ClientFactoryFinalizer at(String address, int port) {
		ClientStart clientStart = ClientStart.at(address, port);
		return use(clientStart);
	}

	@Override
	public ClientFactoryFinalizer asNIO(String address, int port) {
		ClientStart clientStart = ClientStart.nio(address, port);
		return use(clientStart);
	}

	@Override
	public ClientFactoryFinalizer asTCP(String address, int port) {
		ClientStart clientStart = ClientStart.tcp(address, port);
		return use(clientStart);
	}

	@Override
	public ClientFactoryFinalizer asUDP(String address, int port) {
		ClientStart clientStart = ClientStart.udp(address, port);
		return use(clientStart);
	}

	@Override
	public ClientFactoryFinalizer as(String address, int port, ClientCore clientCore) {
		ClientStart clientStart = ClientStart.as(new InetSocketAddress(address, port), clientCore);
		return use(clientStart);
	}
}
