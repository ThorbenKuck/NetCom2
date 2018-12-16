package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.network.client.ClientCore;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;

public class NativeLazyClientFactory implements LazyClientFactory {

	private final GeneratedRepository repository;
	private final Value<ObjectRepository> objectRepositoryValue = Value.emptySynchronized();

	public NativeLazyClientFactory(GeneratedRepository repository) {
		this.repository = repository;
	}

	@Override
	public ClientFactory use(ObjectRepository objectRepository) {
		return null;
	}

	@Override
	public ClientStart at(String address, int port) {
		return null;
	}

	@Override
	public ClientStart asNIO(String address, int port) {
		return null;
	}

	@Override
	public ClientStart asTCP(String address, int port) {
		return null;
	}

	@Override
	public ClientStart asUDP(String address, int port) {
		return null;
	}

	@Override
	public ClientStart as(String address, int port, ClientCore clientCore) {
		return null;
	}
}
