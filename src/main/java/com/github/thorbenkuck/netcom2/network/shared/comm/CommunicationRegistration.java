package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

@Synchronized
public interface CommunicationRegistration {

	static CommunicationRegistration create() {
		return new DefaultCommunicationRegistration();
	}

	<T> ReceivePipeline<T> register(Class<T> clazz);

	void unRegister(Class clazz);

	boolean isRegistered(Class clazz);

	@SuppressWarnings ("unchecked")
	<T> void trigger(Class<T> clazz, Connection connection, Session session, Object o) throws CommunicationNotSpecifiedException;

	void addDefaultCommunicationHandler(OnReceiveSingle<Object> defaultCommunicationHandler);

	void addDefaultCommunicationHandler(OnReceive<Object> defaultCommunicationHandler);

	void addDefaultCommunicationHandler(OnReceiveTriple<Object> defaultCommunicationHandler);

	void clear();

	void clearAllEmptyPipelines();
}