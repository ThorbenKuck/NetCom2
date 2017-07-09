package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.annotations.Experimental;
import de.thorbenkuck.netcom2.network.client.DefaultSynchronize;
import de.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import de.thorbenkuck.netcom2.network.interfaces.SendingService;
import de.thorbenkuck.netcom2.network.shared.*;
import de.thorbenkuck.netcom2.network.shared.comm.model.Acknowledge;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Experimental
public class TCPDefaultConnection extends AbstractConnection {

	private final Map<Class, Synchronize> mapping = new HashMap<>();
	private final Lock communicationLock = new ReentrantLock();

	protected TCPDefaultConnection(Socket socket, SendingService sendingService, ReceivingService receivingService, Session session, Class<?> key) {
		super(socket, sendingService, receivingService, session, key);
	}

	@Override
	protected void onClose() {

	}

	@Override
	protected synchronized void beforeSend(Object object) {
		Objects.requireNonNull(object);
		if(object.getClass().equals(Acknowledge.class)) {
			logging.trace("[TCP] No need to setup an synchronization mechanism an Acknowledge!");
			return;
		}
		logging.trace("[TCP] Locking access to send ..");
		communicationLock.lock();
		logging.debug("[TCP] Preparing send of " + object + " at Thread " + Thread.currentThread());
		Synchronize synchronize = new DefaultSynchronize();
		logging.trace("[TCP] Mapping synchronization mechanism ..");
		synchronized(mapping) {
			mapping.put(object.getClass(), synchronize);
		}
		logging.trace("[TCP] Setting up CallBack ..");
		receivingService.addReceivingCallback(new TCPAckCallBack(object.getClass()));
	}

	@Override
	protected void afterSend(Object object) {
		if(object.getClass().equals(Acknowledge.class)) {
			return;
		}
		logging.debug("[TCP] Preparing receive of Acknowledge from " + object + " at Thread " + Thread.currentThread());
		logging.trace("[TCP] Grabbing Synchronization mechanism ..");
		Synchronize synchronize;
		synchronized (mapping) {
			 synchronize = mapping.get(object.getClass());
		}
		try {
			logging.debug("[TCP] Awaiting synchronization of " + synchronize);
			synchronize.synchronize();
			logging.debug("[TCP] Received Acknowledge of " + object.getClass());
		} catch (InterruptedException e) {
			logging.error("[TCP] Interrupted while synchronizing ", e);
		} finally {
			logging.debug("[TCP] Releasing CommunicationLock");
			communicationLock.unlock();
		}
		logging.trace("[TCP] Continuing Communication after " + object.getClass());
	}

	/**
	 * TODO Correct hashing of send object
	 * {@inheritDoc}
	 */
	@Override
	protected void receivedObject(Object o) {
		logging.debug("[TCP] Testing " + o);
		if(o.getClass().equals(Acknowledge.class)) {
			logging.debug("[TCP] Received Acknowledge " + o);
			ack((Acknowledge) o);
		} else {
			sendAck(o);
		}
	}

	private void sendAck(Object o) {
		logging.debug("[TCP] Acknowledging " + o.getClass());
		write(new Acknowledge(o.getClass()));
	}

	private void ack(Acknowledge acknowledge) {
		logging.debug("[TCP] Grabbing Synchronization mechanism for " + acknowledge.getOf());
		Synchronize synchronize;
		synchronized (mapping) {
			synchronize = mapping.get(acknowledge.getOf());
		}
		if(synchronize == null) {
			logging.error("[TCP] ![DEAD ACKNOWLEDGE]! Found NO Waiting Communication for received Acknowledge " + acknowledge.getOf() + "!");
			return;
		}

		logging.trace("[TCP] Releasing waiting Threads after " + acknowledge.getOf());
		synchronize.goOn();
	}

	private class TCPAckCallBack implements CallBack<Object> {

		private Class<?> hint;
		private boolean removable = false;

		private TCPAckCallBack(Class<?> hint) {
			this.hint = hint;
		}

		@Override
		public void accept(Object o) {
			receivedObject(o);
			removable = true;
		}

		@Override
		public boolean isAcceptable(Object o) {
			return o != null && o.getClass().equals(Acknowledge.class) && ((Acknowledge)o).getOf().equals(hint);
		}

		@Override
		public boolean isRemovable() {
			return removable;
		}

		@Override
		public String toString() {
			return "TCPAckCallBack{hint=" + hint + ", removable=" + removable + "}";
		}
	}
}
