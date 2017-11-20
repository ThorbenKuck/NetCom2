package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.interfaces.SoftStoppable;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

public interface ReceivingService extends Runnable, SoftStoppable {

	void cleanUpCallBacks();

	void addReceivingCallback(final Callback<Object> callback);

	void setup(final Connection connection, final Session session);

	void setSession(final Session session);

	void onDisconnect(final Runnable runnable);

	Awaiting started();
}
