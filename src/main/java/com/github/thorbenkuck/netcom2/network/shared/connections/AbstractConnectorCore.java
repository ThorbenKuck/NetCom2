package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.server.ClientFactory;
import com.github.thorbenkuck.netcom2.network.server.ConnectorCore;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConnectorCore implements ConnectorCore {

	private final Value<EventLoop> currentEventLoopValue = Value.emptySynchronized();
	private final List<EventLoop> eventLoopList = new ArrayList<>();
	private final Value<Integer> maxEventLoopWorkload = Value.synchronize(1024);
	private final Logging logging = Logging.unified();
	private final ClientFactory clientFactory;

	public AbstractConnectorCore(ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	private void establishEventLoop() {
		logging.debug("Creating new EventLoop");
		logging.trace("Opening new NIOEventLoop ..");
		EventLoop eventLoop = createEventLoop();

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

	private void findNextEventLoop() {
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
		for (EventLoop eventLoop : copy) {
			if (eventLoop.workload() < maxEventLoopWorkload.get()) {
				logging.debug("Found EventLoop with capacities.");
				logging.trace("Setting CurrentEventLoopValue");
				currentEventLoopValue.set(eventLoop);
				return;
			}
		}

		logging.debug("Could not locate suitable EventLoop. Requesting creation of a new EventLoop ..");
		establishEventLoop();
	}

	protected abstract EventLoop createEventLoop();

	protected abstract void close() throws IOException;

	protected Client createClient() {
		return clientFactory.produce();
	}

	protected EventLoop getCurrentEventLoop() {
		checkEventLoop();
		return currentEventLoopValue.get();
	}

	protected void checkEventLoop() {
		if (currentEventLoopValue.isEmpty()) {
			logging.trace("EventLoop value is empty. Requesting new EventLoopValue ..");
			establishEventLoop();
		}
		EventLoop current = currentEventLoopValue.get();
		if (current.workload() >= maxEventLoopWorkload.get()) {
			logging.trace("EventLoop value is maxed out. Requesting find of next EventLoop value ..");
			findNextEventLoop();
		}
	}

	@Override
	public final void disconnect() {
		synchronized (eventLoopList) {
			for (EventLoop eventLoop : eventLoopList) {
				eventLoop.shutdownNow();
			}
			logging.debug("Clearing Stored EventLoops");
			eventLoopList.clear();
			currentEventLoopValue.clear();
		}
		try {
			close();
		} catch (IOException e) {
			logging.catching(e);
		}
	}
}
