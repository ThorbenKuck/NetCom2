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

	private final Selector selector;
	private final Queue<RegistrationRequest> requests = new LinkedList<>();
	private final ReadRunnable READ_RUNNABLE = new ReadRunnable();
	private final Logging logging = Logging.unified();
	private final Map<Integer, Consumer<SelectionKey>> operationCallback = new HashMap<>();

	NativeSelectorChannel(Selector selector) {
		this.selector = selector;
		logging.instantiated(this);
	}

	private void add(RegistrationRequest registrationRequest) {
		synchronized (requests) {
			requests.add(registrationRequest);
		}
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
		Awaiting shutdown = READ_RUNNABLE.stop();
		selector.wakeup();
		try {
			shutdown.synchronize();
		} catch (InterruptedException e) {
			logging.catching(e);
		}
		selector.close();
	}

	@Override
	public boolean isRunning() {
		return READ_RUNNABLE.isRunning();
	}

	@Override
	public void start() {
		NetComThreadPool.submitCustomWorkerThread(READ_RUNNABLE);
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

		@Override
		public void run() {
			running.set(true);
			shutdownSynchronize.reset();

			while (isRunning()) {
				try {
					logging.trace(NIOUtils.convertForNIOLog("Awaiting Selector action .."));
					final int selected = selector.select();
					// This check is done, to
					// provide the function of
					// gracefully shutting
					// this runnable down
					// without any Exception
					if (isRunning()) {
						if(!requests.isEmpty()) {
							logging.debug(NIOUtils.convertForNIOLog("Found new registration requests"));
							final Queue<RegistrationRequest> copy;
							synchronized (requests) {
								copy = new LinkedList<>(requests);
							}
							handleRegistration(copy);
						}
						if(selected != 0) {
							logging.debug(NIOUtils.convertForNIOLog("Selector IO action is valid!"));
							handleSelect(selector.selectedKeys());
						}
					}
				} catch (IOException e) {
					if (isRunning()) {
						logging.catching(e);
					}
				}
			}

			running.set(false);
			shutdownSynchronize.goOn();
		}

		// Wow... just... wow...
		// How the hell could you
		// pass something like
		// this, but srsly fight
		// about java 9s Jicksaw..
		// You hypocrites.. bullshit
		@SuppressWarnings("MagicConstant")
		private void handleRegistration(Queue<RegistrationRequest> copy) {
			while(copy.peek() != null) {
				final RegistrationRequest registrationRequest = copy.poll();
				final SocketChannel socketChannel = registrationRequest.getSocketChannel();
				try {
					if(socketChannel.isOpen()) {
						socketChannel.register(selector, registrationRequest.getOp());
					}
				} catch (ClosedChannelException e) {
					logging.error("Channel was already closed!", e);
				}
			}
		}

		private void handleSelect(Set<SelectionKey> selectionKeys) {
			logging.debug("Handling select ..");
			final Iterator<SelectionKey> iterator = selectionKeys.iterator();
			while (iterator.hasNext()) {
				final SelectionKey key = iterator.next();
				iterator.remove();

				if(!key.isValid()) {
					continue;
				}

				int ops;

				// srsly, does anyone have a better idea?
				// I don't get the basic idea behind this
				// stupid constants int to boolean conversion..
				// Maybe this is possible:
				// int ops = key.readyOps();
				if(key.isAcceptable()) {
					ops = SelectionKey.OP_ACCEPT;
				} else if(key.isConnectable()) {
					ops = SelectionKey.OP_CONNECT;
				} else if(key.isReadable()) {
					ops = SelectionKey.OP_READ;
				} else {
					ops = SelectionKey.OP_WRITE;
				}

				synchronized (operationCallback) {
					operationCallback.getOrDefault(ops, selected -> logging.warn("Unhandled key: " + selected)).accept(key);
				}
			}
		}

		public boolean isRunning() {
			return running.get() && selector.isOpen();
		}

		public Awaiting stop() {
			running.set(false);
			return shutdownSynchronize;
		}
	}

	private final class RegistrationRequest {

		private final SocketChannel socketChannel;
		private final int op;

		private RegistrationRequest(SocketChannel socketChannel, int op) {
			this.socketChannel = socketChannel;
			this.op = op;
		}

		public SocketChannel getSocketChannel() {
			return socketChannel;
		}

		public int getOp() {
			return op;
		}
	}
}
