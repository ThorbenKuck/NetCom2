package de.thorbenkuck.netcom2.network.interfaces;

import de.thorbenkuck.netcom2.interfaces.SoftStoppable;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.CallBack;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;

public interface ReceivingService extends Runnable, SoftStoppable {

	void addReceivingCallback(CallBack<Object> callBack);

	void setup(Connection connection, Session session);

	void setSession(Session session);

	void onDisconnect(Runnable runnable);

	Awaiting started();
}
