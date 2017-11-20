package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.interfaces.Adapter;

@FunctionalInterface
public interface DecryptionAdapter extends Adapter<String, String> {

	/**
	 * Creates an default DecryptionAdapter. The call of this Method is to be preferred over using whatever is stated
	 * inside this method. Therefor, only this Method has to be changed, if the default behavior changes
	 *
	 * @return a new, default instance of the DecryptionAdapter instance
	 */
	static DecryptionAdapter getDefault() {
		return s -> s;
	}

}
