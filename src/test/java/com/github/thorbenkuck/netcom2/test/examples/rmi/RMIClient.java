package com.github.thorbenkuck.netcom2.test.examples.rmi;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.ClientStart;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;
import com.github.thorbenkuck.netcom2.utility.NetComThread;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RMIClient implements Runnable {

	private ClientStart clientStart = ClientStart.at("localhost", 4444);
	private RemoteTestInterface test;
	private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	public static void main(String[] args) {
		RMIClient client = new RMIClient();
		NetCom2Utils.runLater(() -> {
			Scanner reader = new Scanner(System.in);  // Reading from System.in
			String entered = "";
			while(!entered.equals("stop")) {
				System.out.println("Awaiting input..");
				entered = reader.nextLine();
				if(!entered.equals("stop")) {
					client.execute();
				}
			}
			reader.close();
		});
		client.run();
	}

	@Override
	public void run() {
		clientStart.addDisconnectedHandler(client -> scheduleReconnect());
		try {
			clientStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
			return;
		}

//		// first instance. Is used if nothing else is set
//		clientStart.getRemoteObjectFactory().setDefaultFallback(() -> {
//			System.out.println("This is the default fallback");
//		});
//
//		// second instance. Is used, if no fallback instance is set
//		clientStart.getRemoteObjectFactory().setFallback(RemoteTestInterface.class, () -> {
//			System.out.println("This is the RemoteTestInterface specific default");
//		});
//
//		// third instance. Is used, if not custom fallback is set on creation
//		clientStart.getRemoteObjectFactory().setFallbackInstance(RemoteTestInterface.class, new LocalRemoteImpl());
//
//		// fourth instance. Overrides the other three fallbacks.
//		test = clientStart.getRemoteObjectFactory().create(RemoteTestInterface.class, (Runnable) () -> {
//			System.out.println("This should override everything!");
//		});


		test = clientStart.getRemoteObjectFactory().create(RemoteTestInterface.class);

		System.out.println(test.getHelloWorld());
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

	public void execute() {
		System.out.println(test.getHelloWorld());
	}

	private class LocalRemoteImpl implements RemoteTestInterface {
		@Override
		public String getHelloWorld() {
			return "Server not reachable!";
		}
	}
}