package de.thorbenkuck.netcom2.network.interfaces;

import de.thorbenkuck.netcom2.interfaces.SoftStoppable;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.CallBack;

import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public interface SendingService extends Runnable, SoftStoppable {
	void addSendDoneCallback(CallBack<Object> callback);

	void overrideSendingQueue(BlockingQueue<Object> linkedBlockingQueue);

	void setup(OutputStream outputStream, BlockingQueue<Object> toSendFrom);

	Awaiting started();
}
