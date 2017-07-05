package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.interfaces.Adapter;

@FunctionalInterface
public interface EncryptionAdapter extends Adapter<String, String> {

	static EncryptionAdapter getDefault() {
		return s -> s;
	}

}
