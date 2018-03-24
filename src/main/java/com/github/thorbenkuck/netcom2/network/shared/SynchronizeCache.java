package com.github.thorbenkuck.netcom2.network.shared;

/**
 * This class holds the empty Synchronize instant for use in {@link Synchronize#empty()}.
 *
 * This is done, to preserve memory, as well as to not allow direct access to using developers.
 *
 * @see Synchronize
 * @see Synchronize#empty()
 */
class SynchronizeCache {

	static final Synchronize EMPTY_SYNCHRONIZE = new EmptySynchronize();

}
