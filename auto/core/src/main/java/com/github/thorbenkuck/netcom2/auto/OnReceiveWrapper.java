package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.interfaces.NetworkInterface;

public interface OnReceiveWrapper {

	void apply(NetworkInterface networkInterface, ObjectRepository objectRepository);

}
