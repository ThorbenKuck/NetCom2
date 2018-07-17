package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
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

class NativeNIOConnectorCore implements ConnectorCore {

	private final Value<Selector> selectorValue = Value.emptySynchronized();
	private final Value<ServerSocketChannel> serverSocketChannelValue = Value.emptySynchronized();
	private final Value<Boolean> connected = Value.synchronize(false);
	private final Value<EventLoop> currentEventLoopValue = Value.emptySynchronized();
	private final List<EventLoop> eventLoopList = new ArrayList<>();
	private final Value<Integer> maxEventLoopWorkload = Value.synchronize(1024);
	private final ClientFactory clientFactory;
	private final Logging logging = Logging.unified();

	NativeNIOConnectorCore(ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
		logging.instantiated(this);
	}

	private void createEventLoop() throws IOException {
		logging.debug(loggingPrefix() + "Creating new EventLoop");
		logging.trace(loggingPrefix() + "Opening new NIOEventLoop ..");
		EventLoop eventLoop = EventLoop.openNonBlocking();

		logging.trace(loggingPrefix() + "Adding NIOEventLoop to all EventLoops ..");
		synchronized (eventLoopList) {
			eventLoopList.add(eventLoop);
		}

		logging.trace(loggingPrefix() + "Updating current EventLoop value ..");
		synchronized (currentEventLoopValue) {
			currentEventLoopValue.set(eventLoop);
		}

		eventLoop.start();
	}

	private synchronized void findNextEventLoop() throws IOException {
		logging.debug(loggingPrefix() + "Searching for free EventLoop using first fit.");
		if(currentEventLoopValue.get().workload() < maxEventLoopWorkload.get()) {
			logging.debug(loggingPrefix() + "Current EventLoop has free capacities.");
			return;
		}
		logging.trace(loggingPrefix() + "Creating a snapshot of all EventLoops ..");
		final List<EventLoop> copy;
		synchronized (eventLoopList) {
			copy = new ArrayList<>(eventLoopList);
		}
		logging.trace(loggingPrefix() + "Searching through EventLoop snapshot for first EventLoop with free capacities ..");
		for (EventLoop eventLoop : copy) {
			if (eventLoop.workload() < maxEventLoopWorkload.get()) {
				logging.debug(loggingPrefix() + "Found EventLoop with capacities.");
				logging.trace(loggingPrefix() + "Setting CurrentEventLoopValue");
				currentEventLoopValue.set(eventLoop);
				return;
			}
		}

		logging.debug(loggingPrefix() + "Could not locate suitable EventLoop. Requesting creation of a new EventLoop ..");
		createEventLoop();
	}

	private void registerConnected(SocketChannel socketChannel) throws IOException {
		logging.debug(loggingPrefix() + "Registering newly connected SocketChannel");
		logging.trace(loggingPrefix() + "Constructing connection for socketChannel ..");
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
		connection.hook(client);
		logging.trace(loggingPrefix() + "Checking current EventLoop value ..");
		if(currentEventLoopValue.isEmpty()) {
			logging.trace(loggingPrefix() + "EventLoop value is empty. Requesting new EventLoopValue ..");
			createEventLoop();
		}
		EventLoop current = currentEventLoopValue.get();
		if (current.workload() >= maxEventLoopWorkload.get()) {
			logging.trace(loggingPrefix() + "EventLoop value is maxed out. Requesting find of next EventLoop value ..");
			findNextEventLoop();
		}
		logging.trace(loggingPrefix() + "Registering Connection to EventLoop");
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
		logging.debug(loggingPrefix() + "Trying to establish connection to " + socketAddress);
		logging.trace(loggingPrefix() + "Checking connected flag..");
		if (connected.get()) {
			logging.debug(loggingPrefix() + "Already connected. Returning");
			return;
		}

		logging.trace(loggingPrefix() + "Creating new Selector ..");
		try {
			selectorValue.set(Selector.open());
		} catch (IOException e) {
			logging.error(loggingPrefix() + "Selector opening failed");
			throw new StartFailedException(e);
		}

		try {
			logging.trace(loggingPrefix() + "Opening ServerSocketChannel ..");
			final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			logging.trace(loggingPrefix() + "Configuring ServerSocketChannel as non blocking");
			serverSocketChannel.configureBlocking(false);
			logging.trace(loggingPrefix() + "Binding ServerSocketChannel to " + socketAddress);
			serverSocketChannel.bind(socketAddress);

			logging.trace(loggingPrefix() + "Storing ServerSocketChannel ..");
			serverSocketChannelValue.set(serverSocketChannel);

			serverSocketChannel.register(selectorValue.get(), SelectionKey.OP_ACCEPT);

			logging.trace(loggingPrefix() + "Updating connected flag");
			connected.set(true);
		} catch (IOException e) {
			logging.error(loggingPrefix() + "IO transaction failed. Recovering for new launch attempt ..");
			clear();
			throw new StartFailedException(e);
		}
	}

	@Override
	public synchronized void handleNext() throws ClientConnectionFailedException {
		logging.debug(loggingPrefix() + "Awaiting next SocketChannel connect");
		logging.trace(loggingPrefix() + "Fetching Selector value ..");
		final Selector selector = selectorValue.get();

		final int selected;
		try {
			logging.trace(loggingPrefix() + "Awaiting select of Selector ..");
			selected = selector.select();
			logging.trace(loggingPrefix() + "Selector woke up");
		} catch (IOException e) {
			logging.error(loggingPrefix() + "Selector#select threw IOException!");
			throw new ClientConnectionFailedException(e);
		}

		if (selected == 0) {
			logging.trace(loggingPrefix() + "No selected events.");
			return;
		}

		logging.trace(loggingPrefix() + "Fetching selected keys ..");
		final Set<SelectionKey> keys = selector.selectedKeys();
		logging.trace(loggingPrefix() + "Fetching Iterator ..");
		final Iterator<SelectionKey> iterator = keys.iterator();

		ClientConnectionFailedException exception = null;

		logging.trace(loggingPrefix() + "Checking all keys ..");
		while (iterator.hasNext()) {
			logging.trace(loggingPrefix() + "Fetching next SelectionKey ..");
			SelectionKey element = iterator.next();
			logging.trace(loggingPrefix() + "Checking only for op_accept ..");
			if ((element.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
				// Accept the new connection

				logging.trace(loggingPrefix() + "Fetching ServerSocketChannel (with a fucking cast ..........)");
				final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) element.channel();
				try {
					logging.trace(loggingPrefix() + "Accepting SocketChannel ..");
					final SocketChannel socketChannel = serverSocketChannel.accept();
					logging.trace(loggingPrefix() + "Configuring accepted SocketChannel as non blocking ..");
					socketChannel.configureBlocking(false);

					logging.trace(loggingPrefix() + "Requesting registration of connected SocketChannel");
					registerConnected(socketChannel);
				} catch (IOException e) {
					logging.error(loggingPrefix() + "Encountered Exception while handling new Connect. Continuing until finish..");
					if (exception == null) {
						exception = new ClientConnectionFailedException(e);
					} else {
						exception.addSuppressed(e);
					}
				}
			} else {
				logging.warn(loggingPrefix() + "Found faulty key: " + element + "!");
			}
			iterator.remove();
		}
		logging.debug(loggingPrefix() + "Queried all connected SocketChannels");
	}

	@Override
	public void disconnect() throws IOException {
		synchronized (eventLoopList) {
			for (EventLoop eventLoop : eventLoopList) {
				eventLoop.shutdownNow();
			}
			eventLoopList.clear();
			currentEventLoopValue.clear();
		}
		selectorValue.get().close();
		serverSocketChannelValue.get().close();
	}

	private String loggingPrefix() {
		return "[NIO] : ";
	}
}
