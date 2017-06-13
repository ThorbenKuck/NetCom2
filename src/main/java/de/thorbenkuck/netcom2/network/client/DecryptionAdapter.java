package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.interfaces.Adapter;

public interface DecryptionAdapter extends Adapter<String, String> {

	static DecryptionAdapter getDefault() {
		return s -> s;
	}

}
