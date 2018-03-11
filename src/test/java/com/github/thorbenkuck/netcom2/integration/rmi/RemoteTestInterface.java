package com.github.thorbenkuck.netcom2.integration.rmi;

import com.github.thorbenkuck.netcom2.annotations.remoteObjects.IgnoreRemoteExceptions;

public interface RemoteTestInterface {

	@IgnoreRemoteExceptions(exceptTypes = {IllegalArgumentException.class, IllegalStateException.class})
	String getHelloWorld();

}
