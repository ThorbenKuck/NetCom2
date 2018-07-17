package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.function.Consumer;

class NativeSelectorChannel implements SelectorChannel {

	private final Logging logging = Logging.unified();
	private final Selector selector;
	private final Queue<RegistrationRequest> requests = new LinkedList<>();
	private final ReadRunnable READ_RUNNABLE = new ReadRunnable();
	private final Map<Integer, Consumer<SelectionKey>> operationCallback = new HashMap<>();

	NativeSelectorChannel(Selector selector) {
		this.selector = selector;
		logging.instantiated(this);
	}

	private void add(RegistrationRequest registrationRequest) {
		logging.trace("[SelectorChannel]: Adding new RegistrationRequest");
		logging.trace("[SelectorChannel]: Accessing requests");
		synchronized (requests) {
			logging.trace("[SelectorChannel]: Adding RegistrationRequest to RequestQueue");
			requests.add(registrationRequest);
		}
		logging.debug("[SelectorChannel]: Waking underlying Selector");
		selector.wakeup();
	}

	@Override
	public void registerForReading(SocketChannel socketChannel) {
		add(new RegistrationRequest(socketChannel, SelectionKey.OP_READ));
	}

	@Override
	public void registerForConnection(SocketChannel socketChannel) {
		add(new RegistrationRequest(socketChannel, SelectionKey.OP_CONNECT));
	}

	@Override
	public void registerForWrite(SocketChannel socketChannel) {
		add(new RegistrationRequest(socketChannel, SelectionKey.OP_WRITE));
	}

	@Override
	public void registerForAccept(SocketChannel socketChannel) {
		add(new RegistrationRequest(socketChannel, SelectionKey.OP_ACCEPT));
	}

	@Override
	public void unregister(SocketChannel socketChannel) {
		socketChannel.keyFor(selector).cancel();
	}

	@Override
	public void register(Consumer<SelectionKey> callback, int op) {
		operationCallback.put(op, callback);
	}

	@Override
	public void close() throws IOException {
		logging.debug("[SelectorChannel]: Closing");
		logging.trace("[SelectorChannel]: Requesting stop of ReadRunnable");
		Awaiting shutdown = READ_RUNNABLE.stop();
		logging.trace("[SelectorChannel]: Waking up underlying Selector");
		selector.wakeup();
		try {
			logging.trace("[SelectorChannel]: Awaiting shutdown of ReadRunnable");
			shutdown.synchronize();
			logging.debug("[SelectorChannel]: ReadRunnable shutdown finished");
		} catch (InterruptedException e) {
			logging.catching(e);
		}
		logging.trace("[SelectorChannel]: Accessing RequestQueue");
		synchronized (requests) {
			logging.trace("[SelectorChannel]: Clearing RequestQueue");
			requests.clear();
		}
		logging.trace("[SelectorChannel]: Accessing OperationCallbacks");
		synchronized (operationCallback) {
			logging.trace("[SelectorChannel]: Clearing OperationCallbacks");
			operationCallback.clear();
		}
		logging.trace("[SelectorChannel]: Closing underlying Selector");
		selector.close();
		logging.info("[SelectorChannel]: Closed");
	}

	@Override
	public boolean isRunning() {
		return READ_RUNNABLE.isRunning();
	}

	@Override
	public void start() {
		NetComThreadPool.submitCustomProcess(READ_RUNNABLE);
	}

	@Override
	public void wakeup() {
		selector.wakeup();
	}

	@Override
	public Selector selector() {
		return selector;
	}

	private final class ReadRunnable implements Runnable {

		private final Value<Boolean> running = Value.synchronize(true);
		private final Synchronize shutdownSynchronize = Synchronize.createDefault();

		private ReadRunnable() {
			logging.instantiated(this);
		}

		// Wow... just... wow...
		// How the hell could you
		// pass something like
		// this, but srsly fight
		// about java 9s Jicksaw..
		// You hypocrites.. bullshit
		@SuppressWarnings("MagicConstant")
		private void handleRegistration(Queue<RegistrationRequest> copy) {
			logging.debug("[ReadRunnable]: Handling new registrations");
			while(copy.peek() != null) {
				logging.trace("[ReadRunnable]: Polling next Registration request");
				final RegistrationRequest registrationRequest = copy.poll();
				final SocketChannel socketChannel = registrationRequest.getSocketChannel();
				try {
					logging.trace("[ReadRunnable]: Checking connected SocketChannel");
					if(socketChannel.isOpen()) {
						logging.trace("[ReadRunnable]: SocketChannel is open. Registering socketChannel ..");
						socketChannel.register(selector, registrationRequest.getOp());
					}
				} catch (ClosedChannelException e) {
					logging.error("[ReadRunnable]: Channel was already closed!", e);
				}
			}
		}

		private void handleSelect(Set<SelectionKey> selectionKeys) {
			logging.debug("[ReadRunnable]: Handling select ..");
			logging.trace("[ReadRunnable]: Fetching Iterator");
			final Iterator<SelectionKey> iterator = selectionKeys.iterator();
			logging.trace("[ReadRunnable]: Checking keys");
			while (iterator.hasNext()) {
				logging.trace("[ReadRunnable]: Fetching next key");
				final SelectionKey key = iterator.next();
				logging.trace("[ReadRunnable]: Removing key from Iterator");
				iterator.remove();

				if(!key.isValid()) {
					logging.debug("[ReadRunnable]: Key is invalid! Continuing ..");
					continue;
				}

				int ops;

				// srsly, does anyone have a better idea?
				// I don't get the basic idea behind this
				// stupid constants int to boolean conversion..
				// Maybe this is possible:
				// int ops = key.readyOps() << SomeMagicConstant;
				logging.trace("[ReadRunnable]: Fetching ops");
				if(key.isAcceptable()) {
					ops = SelectionKey.OP_ACCEPT;
				} else if(key.isConnectable()) {
					ops = SelectionKey.OP_CONNECT;
				} else if(key.isReadable()) {
					ops = SelectionKey.OP_READ;
				} else {
					ops = SelectionKey.OP_WRITE;
				}

				logging.trace("[ReadRunnable]: Accessing OperationCallback");
				synchronized (operationCallback) {
					logging.trace("[ReadRunnable]: Accepting OperationCallback");
					operationCallback.getOrDefault(ops, selected -> logging.warn("Unhandled key: " + selected)).accept(key);
				}
			}
		}

		@Override
		public void run() {
			logging.info(NIOUtils.convertForNIOLog("[ReadRunnable]: Starting"));
			logging.trace(NIOUtils.convertForNIOLog("[ReadRunnable]: Updating Running flag"));
			running.set(true);
			logging.trace(NIOUtils.convertForNIOLog("[ReadRunnable]: Resetting ShutdownSynchronize"));
			shutdownSynchronize.reset();

			logging.trace(NIOUtils.convertForNIOLog("[ReadRunnable]: Entering while loop"));
			while (isRunning()) {
				try {
					logging.trace(NIOUtils.convertForNIOLog("[ReadRunnable]: Awaiting Selector action .."));
					final int selected = selector.select();
					// This check is done, to
					// provide the function of
					// gracefully shutting
					// this runnable down
					// without any Exception
					logging.trace(NIOUtils.convertForNIOLog("[ReadRunnable]: Checking if still running"));
					if (isRunning()) {
						logging.trace(NIOUtils.convertForNIOLog("[ReadRunnable]: Still running. Checking request"));
						if (!requests.isEmpty()) {
							logging.debug(NIOUtils.convertForNIOLog("[ReadRunnable]: Found new registration requests"));
							logging.trace(NIOUtils.convertForNIOLog("[ReadRunnable]: Copying results"));
							final Queue<RegistrationRequest> copy;
							synchronized (requests) {
								copy = new LinkedList<>(requests);
							}
							logging.trace(NIOUtils.convertForNIOLog("[ReadRunnable]: Handling registrations."));
							handleRegistration(copy);
						}
						logging.trace(NIOUtils.convertForNIOLog("[ReadRunnable]: Checking for selected Actions"));
						if (selected != 0) {
							logging.debug(NIOUtils.convertForNIOLog("[ReadRunnable]: Found selected keys!"));
							handleSelect(selector.selectedKeys());
						}
					}
				} catch (IOException e) {
					if (isRunning()) {
						logging.catching(e);
						stop();
					}
				}
			}

			logging.trace(NIOUtils.convertForNIOLog("[ReadRunnable]: Stopping"));
			running.set(false);
			logging.trace(NIOUtils.convertForNIOLog("[ReadRunnable]: Releasing waiting Threads"));
			shutdownSynchronize.goOn();
			logging.trace(NIOUtils.convertForNIOLog("[ReadRunnable]: Finished"));
		}

		public boolean isRunning() {
			return running.get() && selector.isOpen();
		}

		public Awaiting stop() {
			logging.debug("[ReadRunnable]: Stopping");
			logging.trace("[ReadRunnable]: Updating running flag");
			running.set(false);
			logging.trace("[ReadRunnable]: Returning Synchronize");
			return shutdownSynchronize;
		}
	}

	private final class RegistrationRequest {

		private final SocketChannel socketChannel;
		private final int op;

		private RegistrationRequest(SocketChannel socketChannel, int op) {
			this.socketChannel = socketChannel;
			this.op = op;
			logging.instantiated(this);
		}

		public SocketChannel getSocketChannel() {
			return socketChannel;
		}

		public int getOp() {
			return op;
		}
	}
}
