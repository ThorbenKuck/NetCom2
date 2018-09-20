package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.network.shared.connections.DefaultConnection;
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

final class NativeNIOConnectorCore implements ConnectorCore {

	private final Value<Selector> selectorValue = Value.emptySynchronized();
	private final Value<ServerSocketChannel> serverSocketChannelValue = Value.emptySynchronized();
	private final Value<Boolean> connected = Value.synchronize(false);
	private final Value<EventLoop> currentEventLoopValue = Value.emptySynchronized();
	private final List<EventLoop> eventLoopList = new ArrayList<>();
	private final Value<Integer> maxEventLoopWorkload = Value.synchronize(1024);
	private final ClientFactory clientFactory;
	private final Logging logging = Logging.unified();

	NativeNIOConnectorCore(final ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
		logging.instantiated(this);
	}

	private void createEventLoop() throws IOException {
		logging.debug("Creating new EventLoop");
		logging.trace("Opening new NIOEventLoop ..");
		final EventLoop eventLoop = EventLoop.openNonBlocking();

		logging.trace("Adding NIOEventLoop to all EventLoops ..");
		synchronized (eventLoopList) {
			eventLoopList.add(eventLoop);
		}

		logging.trace("Updating current EventLoop value ..");
		synchronized (currentEventLoopValue) {
			currentEventLoopValue.set(eventLoop);
		}

		eventLoop.start();
	}

	private synchronized void findNextEventLoop() throws IOException {
		logging.debug("Searching for free EventLoop using first fit.");
		if (currentEventLoopValue.get().workload() < maxEventLoopWorkload.get()) {
			logging.debug("Current EventLoop has free capacities.");
			return;
		}
		logging.trace("Creating a snapshot of all EventLoops ..");
		final List<EventLoop> copy;
		synchronized (eventLoopList) {
			copy = new ArrayList<>(eventLoopList);
		}
		logging.trace("Searching through EventLoop snapshot for first EventLoop with free capacities ..");
		for (final EventLoop eventLoop : copy) {
			if (eventLoop.workload() < maxEventLoopWorkload.get()) {
				logging.debug("Found EventLoop with capacities.");
				logging.trace("Setting CurrentEventLoopValue");
				currentEventLoopValue.set(eventLoop);
				return;
			}
		}

		logging.debug("Could not locate suitable EventLoop. Requesting creation of a new EventLoop ..");
		createEventLoop();
	}

	private void registerConnected(final SocketChannel socketChannel) throws IOException {
		logging.debug("Registering newly connected SocketChannel");
		logging.trace("Constructing connection for socketChannel ..");
		final Connection connection = Connection.nio(socketChannel);
		// Naively assume that this
		// Connection will be the
		// DefaultConnection. This
		// is not right in some cases,
		// but the initial communication-Chain
		// will change this anyways.
		// We simply cannot know, which connection
		// identifier this Connection belongs to
		connection.setIdentifier(DefaultConnection.class);
		final Client client = clientFactory.produce();
		connection.hook(ConnectionContext.combine(client, connection));
		logging.trace("Checking current EventLoop value ..");
		if (currentEventLoopValue.isEmpty()) {
			logging.trace("EventLoop value is empty. Requesting new EventLoopValue ..");
			createEventLoop();
		}
		final EventLoop current = currentEventLoopValue.get();
		if (current.workload() >= maxEventLoopWorkload.get()) {
			logging.trace("EventLoop value is maxed out. Requesting find of next EventLoop value ..");
			findNextEventLoop();
		}
		logging.trace("Registering Connection to EventLoop");
		currentEventLoopValue.get().register(connection);
	}

	@Override
	public final void clear() {
		if (!serverSocketChannelValue.isEmpty()) {
			serverSocketChannelValue.clear();
		}

		if (!selectorValue.isEmpty()) {
			try {
				selectorValue.set(Selector.open());
			} catch (final IOException e) {
				throw new IllegalStateException("Selector Opening failed at clear. This cannot be recovered!");
			}
		}
	}

	@Override
	public final synchronized void establishConnection(final SocketAddress socketAddress) throws StartFailedException {
		logging.debug("Trying to establish connection to " + socketAddress);
		logging.trace("Checking connected flag..");
		if (connected.get()) {
			logging.debug("Already connected. Returning");
			return;
		}

		logging.trace("Creating new Selector ..");
		try {
			selectorValue.set(Selector.open());
		} catch (IOException e) {
			logging.error("Selector opening failed");
			throw new StartFailedException(e);
		}

		try {
			logging.trace("Opening ServerSocketChannel ..");
			final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			logging.trace("Configuring ServerSocketChannel as non blocking");
			serverSocketChannel.configureBlocking(false);
			logging.trace("Binding ServerSocketChannel to " + socketAddress);
			serverSocketChannel.bind(socketAddress);

			logging.trace("Storing ServerSocketChannel ..");
			serverSocketChannelValue.set(serverSocketChannel);

			serverSocketChannel.register(selectorValue.get(), SelectionKey.OP_ACCEPT);

			logging.trace("Updating connected flag");
			connected.set(true);
		} catch (final IOException e) {
			logging.error("IO transaction failed. Recovering for new launch attempt ..");
			clear();
			throw new StartFailedException(e);
		}
	}

	@Override
	public final synchronized void handleNext() throws ClientConnectionFailedException {
		logging.debug("Awaiting next SocketChannel connect");
		logging.trace("Fetching Selector value ..");
		final Selector selector = selectorValue.get();

		final int selected;
		try {
			logging.trace("Awaiting select of Selector ..");
			selected = selector.select();
			logging.trace("Selector woke up");
		} catch (final IOException e) {
			logging.error("Selector#select threw IOException!");
			throw new ClientConnectionFailedException(e);
		}

		if (selected == 0) {
			logging.trace("No selected events.");
			return;
		}

		logging.trace("Fetching selected keys ..");
		final Set<SelectionKey> keys = selector.selectedKeys();
		logging.trace("Fetching Iterator ..");
		final Iterator<SelectionKey> iterator = keys.iterator();

		ClientConnectionFailedException exception = null;

		logging.trace("Checking all keys ..");
		while (iterator.hasNext()) {
			logging.trace("Fetching next SelectionKey ..");
			SelectionKey element = iterator.next();
			logging.trace("Checking only for op_accept ..");
			if ((element.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
				// Accept the new connection

				logging.trace("Fetching ServerSocketChannel (with a fucking cast ..........)");
				final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) element.channel();
				try {
					logging.trace("Accepting SocketChannel ..");
					final SocketChannel socketChannel = serverSocketChannel.accept();
					logging.trace("Configuring accepted SocketChannel as non blocking ..");
					socketChannel.configureBlocking(false);

					logging.trace("Requesting registration of connected SocketChannel");
					registerConnected(socketChannel);
				} catch (final IOException e) {
					logging.error("Encountered Exception while handling new Connect. Continuing until finish..");
					if (exception == null) {
						exception = new ClientConnectionFailedException(e);
					} else {
						exception.addSuppressed(e);
					}
				}
			} else {
				logging.warn("Found faulty key: " + element + "!");
			}
			iterator.remove();
		}
		logging.debug("Queried all connected SocketChannels");
	}

	@Override
	public final void disconnect() {
		synchronized (eventLoopList) {
			for (final EventLoop eventLoop : eventLoopList) {
				eventLoop.shutdownNow();
			}
			eventLoopList.clear();
			currentEventLoopValue.clear();
		}
		try {
			selectorValue.get().close();
		} catch (final IOException e) {
			logging.catching(e);
		}
		try {
			serverSocketChannelValue.get().close();
		} catch (final IOException e) {
			logging.catching(e);
		}
	}

	@Override
	public String toString() {
		return "NIOConnectorCore{" +
				"serverSocketChannelValue=" + serverSocketChannelValue +
				", connected=" + connected +
				", eventLoopList=" + eventLoopList +
				", maxEventLoopWorkload=" + maxEventLoopWorkload +
				", clientFactory=" + clientFactory +
				'}';
	}
}
