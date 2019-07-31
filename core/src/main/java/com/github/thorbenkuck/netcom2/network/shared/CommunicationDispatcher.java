package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;

import java.util.Collection;

public interface CommunicationDispatcher {

	static CommunicationDispatcher onNetComThread() {
		return new AsynchronousCommunicationDispatcher();
	}

	static CommunicationDispatcher onCurrentThread() {
		return new OnThreadCommunicationDispatcher();
	}

	<T> void dispatch(Collection<ReceivePipeline<T>> pipeline, ConnectionContext connectionContext, Session session, T t);

}
