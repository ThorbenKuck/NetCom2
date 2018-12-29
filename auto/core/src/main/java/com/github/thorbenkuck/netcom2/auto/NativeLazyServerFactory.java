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

	private void launch(ServerStart serverStart) {
		objectRepositoryValue.requireNotEmpty();
		repository.apply(serverStart, objectRepositoryValue.get());
	}

	@Override
	public LazyServerFactory use(ObjectRepository objectRepository) {
		objectRepositoryValue.set(objectRepository);
		return this;
	}

	@Override
	public ServerStart at(int port) {
		ServerStart serverStart = ServerStart.at(port);
		launch(serverStart);
		return serverStart;
	}

	@Override
	public ServerStart asNIO(int port) {
		ServerStart serverStart = ServerStart.nio(port);
		launch(serverStart);
		return serverStart;
	}

	@Override
	public ServerStart asTCP(int port) {
		ServerStart serverStart = ServerStart.tcp(port);
		launch(serverStart);
		return serverStart;
	}

	@Override
	public ServerStart asUDP(int port) {
		ServerStart serverStart = ServerStart.udp(port);
		launch(serverStart);
		return serverStart;
	}

	@Override
	public ServerStart as(int port, ConnectorCore connectorCore) {
		ServerStart serverStart = ServerStart.raw(port);
		serverStart.setConnectorCore(connectorCore);
		launch(serverStart);
		return serverStart;
	}
}
