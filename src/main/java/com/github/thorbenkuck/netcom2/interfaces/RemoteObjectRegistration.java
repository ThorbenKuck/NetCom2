package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelRequest;

public interface RemoteObjectRegistration {

	<T> void register(T t);

	<T> void register(Object o, Class<T> identifier);

	<T> void unregister(T t);

	<T> void run(final RemoteAccessCommunicationModelRequest request, final Connection connection);

}
