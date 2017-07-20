package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.interfaces.Adapter;

@FunctionalInterface
public interface EncryptionAdapter extends Adapter<String, String> {

	static EncryptionAdapter getDefault() {
		return s -> s;
	}

}
