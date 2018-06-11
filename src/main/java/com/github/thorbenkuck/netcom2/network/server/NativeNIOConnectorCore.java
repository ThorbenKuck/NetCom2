package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.EventLoop;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

class NativeNIOConnectorCore implements ConnectorCore {

	private final Value<Selector> selectorValue = Value.emptySynchronized();
	private final Value<ServerSocketChannel> serverSocketChannelValue = Value.emptySynchronized();
	private final Value<Boolean> connected = Value.synchronize(false);
	private final Value<EventLoop> currentEventLoopValue = Value.emptySynchronized();
	private final List<EventLoop> eventLoopList = new ArrayList<>();
	private final Value<Integer> maxEventLoopWorkload = Value.synchronize(1024);
	private final ClientFactory clientFactory;

	NativeNIOConnectorCore(ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	private void createEventLoop() throws IOException {
		EventLoop eventLoop = EventLoop.openNIO();

		synchronized (eventLoopList) {
			eventLoopList.add(eventLoop);
		}

		synchronized (currentEventLoopValue) {
			currentEventLoopValue.set(eventLoop);
		}
	}

	private void findNextEventLoop() throws IOException {
		final List<EventLoop> copy;
		synchronized (eventLoopList) {
			copy = new ArrayList<>(eventLoopList);
		}
		for (EventLoop eventLoop : copy) {
			if (eventLoop.workload() < maxEventLoopWorkload.get()) {
				currentEventLoopValue.set(eventLoop);
				return;
			}
		}

		createEventLoop();
	}

	private void registerConnected(SocketChannel socketChannel) throws IOException {
		Connection connection = Connection.nio(socketChannel);
		EventLoop current = currentEventLoopValue.get();
		if (current.workload() >= maxEventLoopWorkload.get()) {
			findNextEventLoop();
		}
		currentEventLoopValue.get().register(connection);
	}

	@Override
	public void clear() {
		if (!serverSocketChannelValue.isEmpty()) {
			serverSocketChannelValue.clear();
		}

		if (!selectorValue.isEmpty()) {
			try {
				selectorValue.set(Selector.open());
			} catch (IOException e) {
				throw new IllegalStateException("Selector Opening failed at clear. This cannot be recovered!");
			}
		}
	}

	@Override
	public synchronized void establishConnection(SocketAddress socketAddress) throws StartFailedException {
		if (connected.get()) {
			return;
		}

		try {
			selectorValue.set(Selector.open());
		} catch (IOException e) {
			throw new StartFailedException(e);
		}

		try {
			final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.bind(socketAddress);

			serverSocketChannelValue.set(serverSocketChannel);

			connected.set(true);
		} catch (IOException e) {
			clear();
			throw new StartFailedException(e);
		}
	}

	@Override
	public synchronized void handleNext() throws ClientConnectionFailedException {
		final Selector selector = selectorValue.get();

		final int selected;
		try {
			selected = selector.select();
		} catch (IOException e) {
			throw new ClientConnectionFailedException(e);
		}

		if (selected == 0) {
			return;
		}

		final Set<SelectionKey> keys = selector.selectedKeys();
		final Iterator<SelectionKey> iterator = keys.iterator();

		ClientConnectionFailedException exception = null;

		while (iterator.hasNext()) {
			SelectionKey element = iterator.next();
			if ((element.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
				// Accept the new connection
				final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) element.channel();
				final SocketChannel socketChannel;
				try {
					socketChannel = serverSocketChannel.accept();
					socketChannel.configureBlocking(false);

					registerConnected(socketChannel);
				} catch (IOException e) {
					if (exception == null) {
						exception = new ClientConnectionFailedException(e);
					} else {
						exception.addSuppressed(e);
					}
				}
			}
			iterator.remove();
		}
	}
}
