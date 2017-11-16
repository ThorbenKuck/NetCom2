package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.network.shared.ListenAndExpect;

class DefaultReceiveOrSendSync implements ReceiveOrSendSynchronization {

	private final Class<?> sendType;
	private final ListenAndExpect send;
	private final ListenAndExpect received;

	DefaultReceiveOrSendSync(final ListenAndExpect send, final ListenAndExpect received, final Class<?> sendType) {
		this.send = send;
		this.received = received;
		this.sendType = sendType;
	}

	@Override
	public void andWaitForReceiving(final Class clazz) throws InterruptedException {
		send.andWaitFor(clazz);
	}

	@Override
	public void andWaitForSendFinished() throws InterruptedException {
		received.andWaitFor(sendType);
	}
}