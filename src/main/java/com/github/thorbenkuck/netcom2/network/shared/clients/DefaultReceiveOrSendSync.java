package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.ListenAndExpect;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * @version 1.0
 * @since 1.0
 */
@APILevel
class DefaultReceiveOrSendSync implements ReceiveOrSendSynchronization {

	private final Class<?> sendType;
	private final ListenAndExpect send;
	private final ListenAndExpect received;

	@APILevel
	DefaultReceiveOrSendSync(final ListenAndExpect send, final ListenAndExpect received, final Class<?> sendType) {
		this.send = send;
		this.received = received;
		this.sendType = sendType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void andWaitForReceiving(final Class clazz) throws InterruptedException {
		send.andWaitFor(clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void andWaitForSendFinished() throws InterruptedException {
		received.andWaitFor(sendType);
	}
}
