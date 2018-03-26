package com.github.thorbenkuck.netcom2.network.shared;

/**
 * This class holds the empty Synchronize instant for use in {@link Synchronize#empty()}.
 * <p>
 * This is done, to preserve memory, as well as to not allow direct access to using developers. However, you need to request
 * this instance via {@link Synchronize#empty()}. Direct access is strictly discouraged
 *
 * @version 1.0
 * @see Synchronize
 * @see Synchronize#empty()
 * @since 1.0
 */
class SynchronizeCache {

	static final Synchronize EMPTY_SYNCHRONIZE = new EmptySynchronize();

}
