package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.network.server.ConnectorCore;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

class NativeLazyServerFactory implements LazyServerFactory {

	private final GeneratedRepository repository;
	private final Value<ObjectRepository> objectRepositoryValue = Value.emptySynchronized();

	NativeLazyServerFactory(GeneratedRepository repository) {
		this.repository = repository;
	}

	@Override
	public ServerFactory use(ObjectRepository objectRepository) {
		return null;
	}

	@Override
	public ServerStart at(int port) {
		return null;
	}

	@Override
	public ServerStart asNIO(int port) {
		return null;
	}

	@Override
	public ServerStart asTCP(int port) {
		return null;
	}

	@Override
	public ServerStart asUDP(int port) {
		return null;
	}

	@Override
	public ServerStart as(int port, ConnectorCore connectorCore) {
		return null;
	}
}
