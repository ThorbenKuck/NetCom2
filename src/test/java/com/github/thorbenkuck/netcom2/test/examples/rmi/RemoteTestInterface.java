package com.github.thorbenkuck.netcom2.test.examples.rmi;

import com.github.thorbenkuck.netcom2.annotations.remoteObjects.IgnoreRemoteExceptions;

public interface RemoteTestInterface {

	@IgnoreRemoteExceptions(exceptTypes = {IllegalArgumentException.class, IllegalStateException.class})
	String getHelloWorld();

}
