package com.github.thorbenkuck.netcom2.test.examples.general;

import com.github.thorbenkuck.netcom2.annotations.remoteObjects.IgnoreRemoteExceptions;
import com.github.thorbenkuck.netcom2.annotations.remoteObjects.SingletonRemoteObject;

import java.io.Serializable;

@SingletonRemoteObject
public interface ParameterReturnTest extends Serializable {

	@IgnoreRemoteExceptions
	String concatAndReturn(String original);

}
