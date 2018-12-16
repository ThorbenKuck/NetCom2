package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.network.server.ConnectorCore;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

import java.util.List;

final class NativeServerFactory implements ServerFactory {

	private final GeneratedRepository repository;
	private final Value<ObjectRepository> objectRepositoryValue = Value.emptySynchronized();

	NativeServerFactory(GeneratedRepository repository) {
		this.repository = repository;
	}

	private void connect(ServerStart serverStart) {
		List<OnReceiveWrapper> wrapperList = repository.getAll();
		wrapperList.forEach(wrapper -> wrapper.apply(serverStart, objectRepositoryValue.get()));
	}

	@Override
	public ServerFactory use(ObjectRepository objectRepository) {
		objectRepositoryValue.set(objectRepository);

		return this;
	}

	@Override
	public ServerFactoryFinalizer use(ServerStart serverStart) {
		connect(serverStart);

		return new NativeServerFactoryFinalizer(serverStart);
	}

	@Override
	public ServerFactoryFinalizer at(int port) {
		ServerStart serverStart = ServerStart.at(port);
		return use(serverStart);
	}

	@Override
	public ServerFactoryFinalizer asNIO(int port) {
		ServerStart serverStart = ServerStart.nio(port);
		return use(serverStart);
	}

	@Override
	public ServerFactoryFinalizer asTCP(int port) {
		ServerStart serverStart = ServerStart.tcp(port);
		return use(serverStart);
	}

	@Override
	public ServerFactoryFinalizer asUDP(int port) {
		ServerStart serverStart = ServerStart.udp(port);
		return use(serverStart);
	}

	@Override
	public ServerFactoryFinalizer as(int port, ConnectorCore connectorCore) {
		ServerStart serverStart = ServerStart.raw(port);
		serverStart.setConnectorCore(connectorCore);
		return use(serverStart);
	}
}
