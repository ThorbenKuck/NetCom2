package com.github.thorbenkuck.netcom2.test.examples.general;

import com.github.thorbenkuck.netcom2.annotations.remoteObjects.IgnoreRemoteExceptions;
import com.github.thorbenkuck.netcom2.annotations.remoteObjects.SingletonRemoteObject;

@SingletonRemoteObject
public interface LoginHandler {

	@IgnoreRemoteExceptions
	boolean login(String userName, String password);

}
