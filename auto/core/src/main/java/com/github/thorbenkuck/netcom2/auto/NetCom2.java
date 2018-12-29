package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;

public final class NetCom2 {

	private static final Value<GeneratedRepository> repositoryValue = Value.emptySynchronized();

	private NetCom2() {
		throw new UnsupportedOperationException("Instantiation");
	}

	private static GeneratedRepository createRepository() {
		GeneratedRepository repository = new GeneratedRepository();
		repository.read();

		return repository;
	}

	private static GeneratedRepository getRepository() {
		if (repositoryValue.isEmpty()) {
			synchronized (NetCom2.class) {
				// Sanity check to compensate
				// race-conditions
				if (!repositoryValue.isEmpty()) {
					return repositoryValue.get();
				}
				GeneratedRepository repository = createRepository();
				repositoryValue.set(repository);
			}
		}
		return repositoryValue.get();
	}

	public static ServerFactory launchServer() {
		return new NativeServerFactory(getRepository());
	}

	public static ClientFactory launchClient() {
		return new NativeClientFactory(getRepository());
	}

	public static LazyServerFactory createServer() {
		return new NativeLazyServerFactory(getRepository());
	}

	public static LazyClientFactory createClient() {
		return new NativeLazyClientFactory(getRepository());
	}

}
