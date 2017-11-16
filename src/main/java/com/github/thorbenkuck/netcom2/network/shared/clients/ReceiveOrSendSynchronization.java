package com.github.thorbenkuck.netcom2.network.shared.clients;

public interface ReceiveOrSendSynchronization {

	void andWaitForReceiving(final Class clazz) throws InterruptedException;

	void andWaitForSendFinished() throws InterruptedException;

}
