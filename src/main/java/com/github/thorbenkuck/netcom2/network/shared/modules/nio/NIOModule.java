package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.NetworkInterface;
import com.github.thorbenkuck.netcom2.network.server.ServerConnectorCore;
import com.github.thorbenkuck.netcom2.network.shared.modules.Module;
import com.github.thorbenkuck.netcom2.network.shared.modules.netpack.NetworkPackageFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Selector;

public class NIOModule implements Module {

	NIOChannelCache channelCache = new NIOChannelCache();

	@Override
	public void applyTo(NetworkInterface networkInterface) {
		networkInterface.apply(NetworkPackageFactory.access()
				.add(new NioServerSocketFactory(channelCache))
				.add(new NioSocketFactory(channelCache))
				.ifServer(serverStart -> {
					try {
						serverStart.setConnectorCore(new InternalConnectorCore());
					} catch (IOException e) {
						e.printStackTrace();
						throw new IllegalStateException("Selector opening failed!");
					}
				})
				.build());
	}

	private final class InternalConnectorCore implements ServerConnectorCore {

		private final Selector selector;

		private InternalConnectorCore() throws IOException {
			selector = Selector.open();
		}

		@Override
		public Socket apply(ServerSocket serverSocket) throws ClientConnectionFailedException {
			try {
				selector.select();
				selector.keys();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
