package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.NetworkInterface;
import com.github.thorbenkuck.netcom2.network.server.ServerConnectorCore;
import com.github.thorbenkuck.netcom2.network.shared.modules.Module;
import com.github.thorbenkuck.netcom2.network.shared.modules.netpack.NetworkPackageFactory;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.function.Consumer;

public class NIOModule implements Module {

	NIOChannelCache channelCache = new NIOChannelCache();
	NIOConnectionCache connectionCache = new NIOConnectionCache();

	@Override
	public void applyTo(NetworkInterface networkInterface) {
		Selectors selectors;
		try {
			selectors = new Selectors();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		networkInterface.apply(NetworkPackageFactory.access()
				.add(new NIOServerSocketFactory(channelCache, selectors))
				.add(new NIOSocketFactory(channelCache, selectors))
				.add(new NIOConnectionFactory(channelCache))
				.ifServer(serverStart -> {
					try {
						InternalConnectorCore connectorCore = new InternalConnectorCore(channelCache, selectors);
						serverStart.setConnectorCore(connectorCore);
					} catch (IOException e) {
						e.printStackTrace();
						throw new IllegalStateException("Selector opening failed!");
					}
				})
				.build());
	}

	private final class InternalConnectorCore implements ServerConnectorCore {

		private final NIOChannelCache channelCache;
		private final Selectors selectors;

		private InternalConnectorCore(NIOChannelCache channelCache, Selectors selectors) throws IOException {
			this.channelCache = channelCache;
			System.out.println();
			this.selectors = selectors;
		}

		public void setup() {
			NetCom2Utils.runOnNetComThread(() -> {
				Selector receiver = selectors.getReceiver();
				while (true) {
					try {
						receiver.select();
						Set<SelectionKey> keys = receiver.selectedKeys();
						System.out.println("NEW SELECTED KEYS! " + keys);
						for (SelectionKey selectionKey : keys) {
							if (selectionKey.isReadable()) {
								SocketChannel client = (SocketChannel) selectionKey.channel();
								ByteBuffer buffer = ByteBuffer.allocate(256);
								client.read(buffer);
								String result = new String(buffer.array()).trim();
								System.out.println("Received " + result);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}

		@Override
		public void apply(ServerSocket serverSocket, Consumer<Socket> consumer) throws ClientConnectionFailedException {
			try {
				Selector selector = selectors.getSelector();
				selector.select();
				Set<SelectionKey> keys = selector.selectedKeys();
				for (SelectionKey selectionKey : keys) {
					if (selectionKey.isAcceptable()) {
						ServerSocketChannel serverSocketChannel = channelCache.getServerSocketChannel(serverSocket);
						SocketChannel socketChannel = serverSocketChannel.accept();
						socketChannel.configureBlocking(false);
						socketChannel.register(selectors.getReceiver(), SelectionKey.OP_READ);
						channelCache.addSocket(socketChannel.socket(), socketChannel);
						consumer.accept(socketChannel.socket());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
