package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;

/**
 * This class is the identifier for the {@link Connection}, which is established by default.
 * <p>
 * Since it is only an Key, instantiation is not needed. Never. At no point. Period.
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
public final class DefaultConnection {
	private DefaultConnection() {
		throw new IllegalStateException("Cannot instantiated");
	}
}
