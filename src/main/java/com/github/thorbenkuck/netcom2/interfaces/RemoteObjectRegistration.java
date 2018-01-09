package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelRequest;

public interface RemoteObjectRegistration {

	<T> void register(T t);

	void register(Object o, Class<?>... identifier);

	<T> void unregister(T t);

	<T> void run(final RemoteAccessCommunicationModelRequest request, final Connection connection) throws RemoteRequestException;

}
