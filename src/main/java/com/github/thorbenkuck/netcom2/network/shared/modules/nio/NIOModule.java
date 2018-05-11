package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.interfaces.NetworkInterface;
import com.github.thorbenkuck.netcom2.network.server.ServerConnectorCore;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.modules.Module;
import com.github.thorbenkuck.netcom2.network.shared.modules.netpack.NetworkPackageFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

public class NIOModule implements Module {

	private final Logging logging = Logging.unified();
	private Value<NetworkInterface> networkInterfaceValue = Value.emptySynchronized();
	private NIOChannelCache channelCache = new NIOChannelCache();
	private NIOConnectionCache connectionCache = new NIOConnectionCache();
	private NIOConnectorCore connectorCore = new NIOConnectorCore(channelCache);
	private Runnable connectedListener = new ServerListener(channelCache, connectorCore::add);
	private Runnable clientListener = new ClientListener(channelCache);
	private Runnable receivedListener = new ReceivedListener(channelCache, connectionCache,
			(object, connection) -> run(networkInterfaceValue.get().getCommunicationRegistration(), connection, object));

	@Override
	public void applyTo(NetworkInterface networkInterface) {
		networkInterface.apply(NetworkPackageFactory.access()
				.add(new NIOServerSocketFactory(connectionCache, channelCache, connectedListener, receivedListener))
				.add(new NIOSocketFactory(channelCache, clientListener, receivedListener))
				.add(new NIOConnectionFactory(channelCache, connectionCache))
				.ifServer(new ServerConsumer())
				.ifClient(new ClientConsumer())
				.build());
	}

	private void run(CommunicationRegistration communicationRegistration, Connection connection, Object message) {
		try {
			communicationRegistration.trigger(connection, connection.getSession(), message);
		} catch (CommunicationNotSpecifiedException e) {
			logging.catching(e);
		}
	}

	private void close(SocketChannel socketChannel) {
		logging.debug("[NIO]: Disconnect detected: " + socketChannel);
		try {
			connectionCache.get(socketChannel).close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final class ServerConsumer implements Consumer<ServerStart> {

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param serverStart the input argument
		 */
		@Override
		public void accept(ServerStart serverStart) {
			networkInterfaceValue.set(serverStart);
			serverStart.setConnectorCore(connectorCore);
		}
	}

	private final class ClientConsumer implements Consumer<ClientStart> {

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param clientStart the input argument
		 */
		@Override
		public void accept(ClientStart clientStart) {
			networkInterfaceValue.set(clientStart);
		}
	}

	private final class NIOConnectorCore implements ServerConnectorCore {

		private final NIOChannelCache channelCache;
		private final BlockingQueue<SocketChannel> connected = new LinkedBlockingDeque<>();

		private NIOConnectorCore(NIOChannelCache channelCache) {
			this.channelCache = channelCache;
		}

		void add(SocketChannel channel) {
			try {
				logging.debug("[NIO, ServerConnectorCore]: Adding new SocketChannel");
				connected.put(channel);
				logging.trace("[NIO, ServerConnectorCore]: Done adding new SocketChannel: " + connected);
			} catch (InterruptedException e) {
				throw new IllegalStateException("Interrupted while setting new SocketChannel!");
			}
		}

		@Override
		public void apply(ServerSocket serverSocket, Consumer<Socket> consumer) throws ClientConnectionFailedException {
			try {
				logging.trace("[NIO, ServerConnectorCore]: Awaiting new SocketChannel (blocking)");
				SocketChannel socketChannel = connected.take();
				logging.debug(connected.toString());
				logging.debug("[NIO, ServerConnectorCore]: Received new SocketChannel: " + socketChannel);
				channelCache.addSocket(socketChannel.socket(), socketChannel);
				logging.trace("[NIO, ServerConnectorCore]: Informing Callback");
				consumer.accept(socketChannel.socket());
			} catch (InterruptedException e) {
				logging.catching(e);
			}
		}
	}
}
