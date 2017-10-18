package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.network.shared.ListenAndExpect;

class DefaultReceiveOrSendSynch implements ReceiveOrSendSynchronization {

	private final Class<?> sendType;
	private ListenAndExpect send;
	private ListenAndExpect received;

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
