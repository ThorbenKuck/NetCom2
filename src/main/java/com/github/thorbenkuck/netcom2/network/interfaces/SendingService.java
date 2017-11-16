package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.interfaces.SoftStoppable;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.CallBack;

import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

public interface SendingService extends Runnable, SoftStoppable {
	void addSendDoneCallback(final CallBack<Object> callback);

	void overrideSendingQueue(final BlockingQueue<Object> linkedBlockingQueue);

	void setup(final OutputStream outputStream, final BlockingQueue<Object> toSendFrom);

	Awaiting started();
}
