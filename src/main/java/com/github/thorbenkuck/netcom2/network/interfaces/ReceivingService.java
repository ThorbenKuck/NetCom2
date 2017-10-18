package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.interfaces.SoftStoppable;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.CallBack;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

public interface ReceivingService extends Runnable, SoftStoppable {

	void cleanUpCallBacks();

	void addReceivingCallback(CallBack<Object> callBack);

	void setup(Connection connection, Session session);

	void setSession(Session session);

	void onDisconnect(Runnable runnable);

	Awaiting started();
}
