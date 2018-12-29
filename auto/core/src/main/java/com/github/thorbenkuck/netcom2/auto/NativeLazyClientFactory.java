package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.network.client.ClientCore;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;

import java.net.InetSocketAddress;

public class NativeLazyClientFactory implements LazyClientFactory {

	private final GeneratedRepository repository;
	private final Value<ObjectRepository> objectRepositoryValue = Value.emptySynchronized();

	public NativeLazyClientFactory(GeneratedRepository repository) {
		this.repository = repository;
	}

	private void setup(ClientStart clientStart) {
		objectRepositoryValue.requireNotEmpty();
		repository.apply(clientStart, objectRepositoryValue.get());
	}

	@Override
	public LazyClientFactory use(ObjectRepository objectRepository) {
		this.objectRepositoryValue.set(objectRepository);
		return this;
	}

	@Override
	public ClientStart at(String address, int port) {
		ClientStart clientStart = ClientStart.at(address, port);
		setup(clientStart);
		return clientStart;
	}

	@Override
	public ClientStart asNIO(String address, int port) {
		ClientStart clientStart = ClientStart.nio(address, port);
		setup(clientStart);
		return clientStart;
	}

	@Override
	public ClientStart asTCP(String address, int port) {
		ClientStart clientStart = ClientStart.tcp(address, port);
		setup(clientStart);
		return clientStart;
	}

	@Override
	public ClientStart asUDP(String address, int port) {
		ClientStart clientStart = ClientStart.udp(address, port);
		setup(clientStart);
		return clientStart;
	}

	@Override
	public ClientStart as(String address, int port, ClientCore clientCore) {
		ClientStart clientStart = ClientStart.as(new InetSocketAddress(address, port), clientCore);
		setup(clientStart);
		return clientStart;
	}
}
