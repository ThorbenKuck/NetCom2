package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Acknowledge;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This ConnectionImplementation attempts to access an TCP Connection. It attempts.. Sryl, can someone please do this?
 *
 * @version 1.0
 * @since 1.0
 */
public class TCPDefaultConnection extends AbstractConnection {

	private final Map<Class, Semaphore> mapping = new HashMap<>();
	private final Lock communicationLock = new ReentrantLock();

	protected TCPDefaultConnection(final Socket socket, final SendingService sendingService,
	                               final ReceivingService receivingService,
	                               final Session session, final Class<?> key) {
		super(socket, sendingService, receivingService, session, key);
	}

	/**
	 * Acknowledges a received {@link Acknowledge}.
	 *
	 * @param acknowledge the received {@link Acknowledge}, that should be acknowledged, because the {@link Acknowledge}
	 *                    is an acknowledge, that acknowledges the receiving of an Object, that is not an {@link Acknowledge}
	 */
	private void ack(final Acknowledge acknowledge) {
		logging.debug("[TCP] Grabbing Synchronization mechanism for " + acknowledge.getOf());
		final Semaphore synchronize;
		synchronized (mapping) {
			synchronize = mapping.get(acknowledge.getOf());
		}
		if (synchronize == null) {
			logging.error("[TCP] ![DEAD ACKNOWLEDGE]! Found NO Waiting Communication for received Acknowledge " +
					acknowledge.getOf() + "!");
			return;
		}

		logging.trace("[TCP] Releasing waiting Threads after " + acknowledge.getOf());
		synchronize.release();
	}

	/**
	 * Send a {@link Acknowledge} for an specific Object.
	 *
	 * @param o the Object, that was send and should be acknowledged
	 */
	private void sendAck(final Object o) {
		logging.debug("[TCP] Acknowledging " + o.getClass());
		write(new Acknowledge(o.getClass()));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the provided Object is null
	 */
	@Override
	protected synchronized void beforeSend(final Object object) {
		NetCom2Utils.parameterNotNull(object);
		if (object.getClass().equals(Acknowledge.class)) {
			logging.trace("[TCP] No need to setup an synchronization mechanism an Acknowledge!");
			return;
		}
		logging.trace("[TCP] Locking access to send ..");
		communicationLock.lock();
		logging.debug("[TCP] Preparing send of " + object + " at Thread " + Thread.currentThread());
		final Semaphore semaphore = new Semaphore(1);
		logging.trace("[TCP] ClientMapping synchronization mechanism ..");
		synchronized (mapping) {
			mapping.put(object.getClass(), semaphore);
		}
		logging.trace("[TCP] Setting up Callback ..");
		receivingService.addReceivingCallback(new TCPAckCallback(object.getClass()));
	}

	/**
	 * TODO Correct hashing of send object
	 * {@inheritDoc}
	 */
	@Override
	protected void receivedObject(final Object o) {
		logging.debug("[TCP] Testing " + o);
		if (o.getClass().equals(Acknowledge.class)) {
			logging.debug("[TCP] Received Acknowledge " + o);
			ack((Acknowledge) o);
		} else {
			sendAck(o);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onClose() {

	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the provided Object is null
	 */
	@Override
	protected void afterSend(final Object object) {
		NetCom2Utils.parameterNotNull(object);
		if (object.getClass().equals(Acknowledge.class)) {
			return;
		}
		logging.debug("[TCP] Preparing receive of Acknowledge from " + object + " at Thread " + Thread.currentThread());
		logging.trace("[TCP] Grabbing Synchronization mechanism ..");
		final Semaphore synchronize;
		synchronized (mapping) {
			synchronize = mapping.get(object.getClass());
		}
		try {
			logging.debug("[TCP] Awaiting synchronization of " + synchronize);
			synchronize.acquire();
			logging.debug("[TCP] Received Acknowledge of " + object.getClass());
		} catch (InterruptedException e) {
			logging.error("[TCP] Interrupted while synchronizing ", e);
		} finally {
			logging.debug("[TCP] Releasing CommunicationLock");
			communicationLock.unlock();
		}
		logging.trace("[TCP] Continuing Communication after " + object.getClass());
	}

	private class TCPAckCallback implements Callback<Object> {

		private final Class<?> hint;
		private boolean removable = false;

		private TCPAckCallback(final Class<?> hint) {
			this.hint = hint;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void accept(final Object o) {
			receivedObject(o);
			removable = true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isAcceptable(final Object o) {
			return o != null && o.getClass().equals(Acknowledge.class) && ((Acknowledge) o).getOf().equals(hint);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isRemovable() {
			return removable;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return "TCPAckCallback{hint=" + hint + ", removable=" + removable + "}";
		}
	}
}
