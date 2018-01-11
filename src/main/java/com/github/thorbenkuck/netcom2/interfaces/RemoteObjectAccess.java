package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.annotations.Experimental;
import com.github.thorbenkuck.netcom2.network.client.InvocationHandlerProducer;

public interface RemoteObjectAccess {

	@Experimental
	<T> T getRemoteObject(Class<T> clazz);

	@Experimental
	void updateRemoteInvocationProducer(InvocationHandlerProducer invocationHandlerProducer);

}
