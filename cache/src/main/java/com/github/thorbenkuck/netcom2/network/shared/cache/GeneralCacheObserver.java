package com.github.thorbenkuck.netcom2.network.shared.cache;

public interface GeneralCacheObserver extends CacheObserver<Object> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	default boolean accept(final Object o) {
		return o != null;
	}

}
