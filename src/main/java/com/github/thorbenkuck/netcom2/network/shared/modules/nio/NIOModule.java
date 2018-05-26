package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.interfaces.NetworkInterface;
import com.github.thorbenkuck.netcom2.network.server.ServerConnectorCore;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.modules.Module;
import com.github.thorbenkuck.netcom2.network.shared.modules.netpack.NetworkPackageFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

public final class NIOModule implements Module {

	private final Logging logging = Logging.unified();
	private final NIOConfig nioConfig;
	private final Value<NetworkInterface> networkInterfaceValue = Value.emptySynchronized();
	private final NIOChannelCache channelCache = new NIOChannelCache();
	private final NIOConnectionCache connectionCache = new NIOConnectionCache();
	private final NIOConnectorCore connectorCore = new NIOConnectorCore(channelCache);
	private final Runnable connectedListener = new NIOServerListener(channelCache, connectorCore::add);
	private final Runnable clientListener = new NIOClientListener(channelCache);
	private final Runnable receivedListener;

	public NIOModule() {
		this(new NIOConfig());
	}

	public NIOModule(final NIOConfig nioConfig) {
		this.nioConfig = nioConfig;
		receivedListener = new NIOReceivedListener(channelCache, connectionCache,
				(object, connection) -> run(networkInterfaceValue.get().getCommunicationRegistration(), connection, object), nioConfig);
	}

	@Override
	public void applyTo(final NetworkInterface networkInterface) {
		logging.debug("[NIO] Applying NIOModule to the provided NetworkInterface");
		networkInterface.apply(NetworkPackageFactory.access()
				.add(new NIOServerSocketFactory(channelCache, connectedListener, receivedListener))
				.add(new NIOSocketFactory(channelCache, clientListener, receivedListener))
				.add(new NIOConnectionFactory(channelCache, connectionCache))
				.ifServer(new ServerConsumer())
				.ifClient(new ClientConsumer())
				.build());

		logging.info("[NIO] Done. Applied NIO to " + networkInterface);
	}

	public NIOConfig getNioConfig() {
		return nioConfig;
	}

	private void run(final CommunicationRegistration communicationRegistration, final NIOConnection connection, final Object message) {
		logging.trace("[NIO] Offering received Object to CommunicationRegistration");
		try {
			communicationRegistration.trigger(connection, connection.getSession(), message);
		} catch (final CommunicationNotSpecifiedException e) {
			logging.catching(e);
		}

		logging.trace("[NIO] Informing NIOConnection about received Objects");
		connection.callbackReceived(message);
	}

	private final class ServerConsumer implements Consumer<ServerStart> {

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param serverStart the input argument
		 */
		@Override
		public void accept(final ServerStart serverStart) {
			logging.debug("[NIO] Server environment detected. Initializing ..");
			logging.trace("[NIO] Setting the internal NetworkInterfaceValue ..");
			networkInterfaceValue.set(serverStart);
			logging.trace("[NIO] Setting ConnectorCore ..");
			serverStart.setConnectorCore(connectorCore);
			logging.debug("[NIO] Server environment initialized.");
		}
	}

	private final class ClientConsumer implements Consumer<ClientStart> {

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param clientStart the input argument
		 */
		@Override
		public void accept(final ClientStart clientStart) {
			logging.debug("[NIO] Client environment detected. Initializing ..");
			logging.trace("[NIO] Setting the internal NetworkInterfaceValue ..");
			networkInterfaceValue.set(clientStart);
			logging.debug("[NIO] Client environment initialized.");
		}
	}

	private final class NIOConnectorCore implements ServerConnectorCore {

		private final NIOChannelCache channelCache;
		private final BlockingQueue<SocketChannel> connected = new LinkedBlockingDeque<>();

		private NIOConnectorCore(final NIOChannelCache channelCache) {
			this.channelCache = channelCache;
		}

		void add(final SocketChannel channel) {
			try {
				logging.debug("[NIO] Adding new SocketChannel");
				connected.put(channel);
				logging.trace("[NIO] Done adding new SocketChannel: " + connected);
			} catch (InterruptedException e) {
				throw new IllegalStateException("Interrupted while setting new SocketChannel!");
			}
		}

		@Override
		public void apply(final ServerSocket serverSocket, final Consumer<Socket> consumer) throws ClientConnectionFailedException {
			try {
				logging.trace("[NIO] Awaiting new SocketChannel (blocking)");
				SocketChannel socketChannel = connected.take();
				logging.debug(connected.toString());
				logging.debug("[NIO] Received new SocketChannel: " + socketChannel);
				channelCache.addSocket(socketChannel.socket(), socketChannel);
				logging.trace("[NIO] Informing Callback");
				consumer.accept(socketChannel.socket());
			} catch (final InterruptedException e) {
				logging.catching(e);
			}
		}
	}
}
