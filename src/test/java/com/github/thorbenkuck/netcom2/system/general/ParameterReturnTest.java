package com.github.thorbenkuck.netcom2.system.general;

import com.github.thorbenkuck.netcom2.annotations.rmi.IgnoreRemoteExceptions;
import com.github.thorbenkuck.netcom2.annotations.rmi.SingletonRemoteObject;

import java.io.Serializable;

@SingletonRemoteObject
public interface ParameterReturnTest extends Serializable {

	@IgnoreRemoteExceptions
	String concatAndReturn(String original);

}
