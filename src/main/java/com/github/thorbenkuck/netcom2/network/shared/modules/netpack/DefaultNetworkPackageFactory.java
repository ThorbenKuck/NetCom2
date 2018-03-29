package com.github.thorbenkuck.netcom2.network.shared.modules.netpack;

import com.github.thorbenkuck.netcom2.interfaces.Factory;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.interfaces.ClientConnectedHandler;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.clients.ConnectionFactory;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.net.ServerSocket;
import java.util.Optional;
import java.util.function.Consumer;

class DefaultNetworkPackageFactory implements NetworkPackageFactory {

	private final InternalNetworkPackage networkPackage = new InternalNetworkPackage();

	@Override
	public NetworkPackageFactory add(SocketFactory socketFactory) {
		NetCom2Utils.parameterNotNull(socketFactory);
		networkPackage.socketFactory = socketFactory;

		return this;
	}

	@Override
	public NetworkPackageFactory add(Factory<Integer, ServerSocket> serverSocketFactory) {
		NetCom2Utils.parameterNotNull(serverSocketFactory);
		networkPackage.serverSocketFactory = serverSocketFactory;

		return this;
	}

	@Override
	public NetworkPackageFactory add(ClientConnectedHandler clientConnectedHandler) {
		NetCom2Utils.parameterNotNull(clientConnectedHandler);
		networkPackage.clientConnectedHandler = clientConnectedHandler;

		return this;
	}

	@Override
	public NetworkPackageFactory add(ConnectionFactory connectionFactory) {
		NetCom2Utils.parameterNotNull(connectionFactory);
		networkPackage.connectionFactory = connectionFactory;

		return this;
	}

	@Override
	public NetworkPackageFactory ifServer(Consumer<ServerStart> consumer) {
		NetCom2Utils.parameterNotNull(consumer);
		networkPackage.serverStartConsumer = consumer;

		return this;
	}

	@Override
	public NetworkPackageFactory ifClient(Consumer<ClientStart> consumer) {
		NetCom2Utils.parameterNotNull(consumer);
		networkPackage.clientStartConsumer = consumer;

		return this;
	}

	@Override
	public NetworkPackage build() {
		return networkPackage;
	}

	private final class InternalNetworkPackage implements NetworkPackage {

		Factory<Integer, ServerSocket> serverSocketFactory;
		SocketFactory socketFactory;
		ClientConnectedHandler clientConnectedHandler;
		ConnectionFactory connectionFactory;
		Consumer<ServerStart> serverStartConsumer = serverStart -> {
		};
		Consumer<ClientStart> clientStartConsumer = serverStart -> {
		};

		@Override
		public Optional<Factory<Integer, ServerSocket>> serverSocketFactory() {
			return Optional.ofNullable(serverSocketFactory);
		}

		@Override
		public Optional<SocketFactory> socketFactory() {
			return Optional.ofNullable(socketFactory);
		}

		@Override
		public Optional<ClientConnectedHandler> clientConnectedHandler() {
			return Optional.ofNullable(clientConnectedHandler);
		}

		@Override
		public Optional<ConnectionFactory> connectionFactory() {
			return Optional.ofNullable(connectionFactory);
		}

		@Override
		public void apply(ServerStart serverStart) {
			serverStartConsumer.accept(serverStart);
		}

		@Override
		public void apply(ClientStart clientStart) {
			clientStartConsumer.accept(clientStart);
		}
	}
}
