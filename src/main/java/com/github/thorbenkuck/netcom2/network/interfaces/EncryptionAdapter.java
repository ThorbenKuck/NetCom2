package com.github.thorbenkuck.netcom2.network.interfaces;

/**
 * This interface is used to encrypt any String.
 * <p>
 * It inherits from the Adapter interface and converts a String into another String.
 * It is utilized within the DefaultSendingService, to
 * encrypt messages received over the Network.
 *
 * @version 1.0
 * @since 1.0
 * @deprecated This class will be removed in V.1.2. All Methods have been removed. use {@link com.github.thorbenkuck.netcom2.network.shared.EncryptionAdapter}
 */
@Deprecated
public interface EncryptionAdapter {
}
