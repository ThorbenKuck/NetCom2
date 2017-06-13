package de.thorbenkuck.netcom2.network.interfaces;

import de.thorbenkuck.netcom2.interfaces.SoftStoppable;
import de.thorbenkuck.netcom2.network.shared.Awaiting;

import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

public interface SendingService extends Runnable, SoftStoppable {
	void overrideSendingQueue(LinkedBlockingQueue<Object> linkedBlockingQueue);

	void setup(OutputStream outputStream, LinkedBlockingQueue<Object> toSendFrom);

	Awaiting started();
}
