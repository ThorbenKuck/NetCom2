package com.github.thorbenkuck.netcom2.network.shared.cache;

/**
 * A GeneralCacheObserver is a CacheObserver that accepts all Objects...
 *
 * @version 1.0
 * @since 1.0
 */
public interface GeneralCacheObserver extends CacheObserver<Object> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	default boolean accept(final Object o) {
		return o != null;
	}

}
