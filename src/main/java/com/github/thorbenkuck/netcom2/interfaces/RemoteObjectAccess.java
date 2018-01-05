package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.annotations.Experimental;

public interface RemoteObjectAccess {

	@Experimental
	<T> T getRemoteObject(Class<T> clazz);

}
