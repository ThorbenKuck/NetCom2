package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.interfaces.Adapter;

/**
 * This interface is used to encrypt any String.
 * <p>
 * It inherits from the Adapter interface and converts a String into another String.
 * It is utilized within the DefaultSendingService, to
 * encrypt messages received over the Network.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface EncryptionAdapter extends Adapter<String, String> {

	/**
	 * Returns a new, default Encryption adapter.
	 * <p>
	 * If you need the Default for any reason, it is recommended to uses this instead of hardcoding it
	 *
	 * @return the EncryptionAdapter that is by default used within NetCom2
	 */
	static EncryptionAdapter getDefault() {
		return s -> s;
	}

}
