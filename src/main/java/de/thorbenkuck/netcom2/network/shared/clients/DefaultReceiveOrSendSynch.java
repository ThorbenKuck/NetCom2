package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.network.shared.ListenAndExpect;

class DefaultReceiveOrSendSynch implements ReceiveOrSendSynchronization {

	private ListenAndExpect send;
	private ListenAndExpect received;
	private final Class<?> sendType;

	DefaultReceiveOrSendSynch(ListenAndExpect send, ListenAndExpect received, Class<?> sendType) {
		this.send = send;
		this.received = received;
		this.sendType = sendType;
	}

	@Override
	public void andWaitForReceiving(Class clazz) throws InterruptedException {
		send.andWaitFor(clazz);
	}

	@Override
	public void andWaitForSendFinished() throws InterruptedException {
		received.andWaitFor(sendType);
	}
}
