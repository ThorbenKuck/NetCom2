package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import com.github.thorbenkuck.netcom2.interfaces.Mutex;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

import java.util.List;
import java.util.Map;

@Synchronized
public interface CommunicationRegistration extends Mutex {

	static CommunicationRegistration create() {
		return new DefaultCommunicationRegistration();
	}

	<T> ReceivePipeline<T> register(final Class<T> clazz);

	void unRegister(final Class clazz);

	boolean isRegistered(final Class clazz);

	@SuppressWarnings("unchecked")
	<T> void trigger(final Class<T> clazz, final Connection connection, final Session session, final Object o)
			throws CommunicationNotSpecifiedException;

	void addDefaultCommunicationHandler(final OnReceiveSingle<Object> defaultCommunicationHandler);

	void addDefaultCommunicationHandler(final OnReceive<Object> defaultCommunicationHandler);

	void addDefaultCommunicationHandler(final OnReceiveTriple<Object> defaultCommunicationHandler);

	void clear();

	void clearAllEmptyPipelines();

	void updateBy(final CommunicationRegistration communicationRegistration);

	Map<Class, ReceivePipeline<?>> map();

	List<OnReceiveTriple<Object>> listDefaultsCommunicationRegistration();
}
