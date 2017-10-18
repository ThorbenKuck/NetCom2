package com.github.thorbenkuck.netcom2.network.shared.cache;

public interface GeneralCacheObserver extends CacheObserver<Object> {

	@Override
	default boolean accept(Object o) {
		return o != null;
	}

}
