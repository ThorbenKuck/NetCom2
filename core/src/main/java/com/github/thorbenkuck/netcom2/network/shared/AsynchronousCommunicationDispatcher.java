package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.util.Collection;

class AsynchronousCommunicationDispatcher implements CommunicationDispatcher {

	private final Logging logging = Logging.unified();

	@Override
	public <T> void dispatch(Collection<ReceivePipeline<T>> pipeline, ConnectionContext connectionContext, Session session, T t) {
		logging.debug("Dispatching triggering asynchronous");
		NetComThreadPool.submitCustomProcess(new DispatcherTask<>(pipeline, t, connectionContext, session));
	}
}
