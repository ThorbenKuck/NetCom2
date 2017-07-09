package de.thorbenkuck.netcom2.network.shared.clients;

public interface ReceiveOrSendSynchronization {

	void andWaitForReceiving(Class clazz) throws InterruptedException;

	void andWaitForSendFinished() throws InterruptedException;

}
