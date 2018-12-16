package com.github.thorbenkuck.netcom2.auto;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

class GeneratedRepository {

	private final List<OnReceiveWrapper> cache = new ArrayList<>();

	final synchronized void read() {
		ServiceLoader<OnReceiveWrapper> onReceiveWrappers = ServiceLoader.load(OnReceiveWrapper.class);
		synchronized (cache) {
			onReceiveWrappers.forEach(cache::add);
		}
	}

	final List<OnReceiveWrapper> getAll() {
		synchronized (cache) {
			return new ArrayList<>(cache);
		}
	}
}
