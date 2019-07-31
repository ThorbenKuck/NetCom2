package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;

import java.util.Collection;

class OnThreadCommunicationDispatcher implements CommunicationDispatcher {
	@Override
	public <T> void dispatch(Collection<ReceivePipeline<T>> pipeline, ConnectionContext connectionContext, Session session, T t) {
		new DispatcherTask<>(pipeline, t, connectionContext, session).run();
	}
}
