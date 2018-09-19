package com.github.thorbenkuck.netcom2.integration.rmi;

import com.github.thorbenkuck.netcom2.annotations.rmi.IgnoreRemoteExceptions;

public interface RemoteTestInterface {

	@IgnoreRemoteExceptions(exceptTypes = {IllegalArgumentException.class, IllegalStateException.class})
	String getHelloWorld();

}
