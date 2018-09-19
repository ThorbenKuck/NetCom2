package com.github.thorbenkuck.netcom2.integration.rmi;

import com.github.thorbenkuck.netcom2.exceptions.RemoteObjectNotRegisteredException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.modules.ModuleFactory;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RMIClient implements Runnable {

	private ClientStart clientStart = ClientStart.at("localhost", 4444);
	private RemoteTestInterface test;
	private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.trace());
		RMIClient client = new RMIClient();
		new Thread(() -> {
			Scanner reader = new Scanner(System.in);  // Reading from System.in
			String entered = "";
			while (!"stop".equals(entered)) {
				System.out.println("Awaiting input..");
				entered = reader.nextLine();
				if (!"stop".equals(entered)) {
					try {
						client.trigger();
					} catch (RemoteObjectNotRegisteredException e) {
						e.printStackTrace(System.out);
					}
				}
			}
			reader.close();
		}).start();
		client.run();
	}

	private void scheduleReconnect() {
		System.out.println("Schedule reconnect..");
		executorService.schedule(() -> {
			try {
				clientStart.launch();
				System.out.println("Reconnected!");
			} catch (StartFailedException e) {
				System.out.println("Reconnect failed..");
				scheduleReconnect();
			}
		}, 6, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		ModuleFactory.access()
				.nio()
				.setBufferSize(1024)
				.apply(clientStart);

		clientStart.addDisconnectedHandler(client -> scheduleReconnect());
		try {
			clientStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
			return;
		}

		// first instance. Is used if nothing else is set
		clientStart.getRemoteObjectFactory().setDefaultFallback(() -> {
			System.out.println("This is the default fallback");
		});

		// second instance. Is used, if no fallback instance is set
		clientStart.getRemoteObjectFactory().setFallback(RemoteTestInterface.class, () -> {
			System.out.println("This is the RemoteTestInterface specific default");
		});

		// third instance. Is used, if not custom fallback is set on creation
		clientStart.getRemoteObjectFactory().setFallbackInstance(RemoteTestInterface.class, new LocalRemoteImpl());

		// fourth instance. Overrides the other three fallbacks.
		test = clientStart.getRemoteObjectFactory().create(RemoteTestInterface.class, new LocalRemoteImpl());


		test = clientStart.getRemoteObjectFactory().create(RemoteTestInterface.class);

		PrimitiveRemoteTestInterface primitiveRemoteTestInterface = clientStart.getRemoteObject(PrimitiveRemoteTestInterface.class);
		System.out.println(primitiveRemoteTestInterface.get(11));
	}

	public void trigger() {
		System.out.println(test.getHelloWorld());
	}

	private class LocalRemoteImpl implements RemoteTestInterface {
		@Override
		public String getHelloWorld() {
			return "Server not reachable!";
		}
	}
}