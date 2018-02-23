package com.github.thorbenkuck.netcom2.test.examples.rmi;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.ClientStart;

public class RMIClient implements Runnable {

	private ClientStart clientStart = ClientStart.at("localhost", 4444);

	public static void main(String[] args) {
		new RMIClient().run();
	}

	@Override
	public void run() {
		try {
			clientStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
			return;
		}

		// first instance. Is used if nothing else is set
		clientStart.getRemoteObjectFactory().addFallback(() -> {
			throw new IllegalStateException("Das ist default.");
		});
		// second instance. Is used, if no fallback instance is set
		clientStart.getRemoteObjectFactory().addFallback(RemoteTestInterface.class, (Runnable) () -> {
			throw new IllegalStateException("Das ist der RemoteTestInterface default");
		});
		// third instance. Is used, if not custom fallback is set on creation
		clientStart.getRemoteObjectFactory().addFallback(RemoteTestInterface.class, new LocalRemoteImpl());
		// fourth instance. Overrides the other three fallbacks.
		RemoteTestInterface test = clientStart.getRemoteObjectFactory().create(RemoteTestInterface.class, (Runnable) () -> {
			throw new IllegalStateException("This should override everything!");
		});
		System.out.println(test.getHelloWorld());
	}

	private class LocalRemoteImpl implements RemoteTestInterface {
		@Override
		public String getHelloWorld() {
			return "Server not reachable!";
		}
	}
}