package com.github.thorbenkuck.netcom2.integration.general;

import com.github.thorbenkuck.netcom2.annotations.rmi.IgnoreRemoteExceptions;
import com.github.thorbenkuck.netcom2.annotations.rmi.SingletonRemoteObject;

@SingletonRemoteObject
public interface LoginHandler {

	@IgnoreRemoteExceptions
	boolean login(String userName, String password);

}
