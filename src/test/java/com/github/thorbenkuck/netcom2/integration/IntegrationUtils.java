package com.github.thorbenkuck.netcom2.integration;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;

public class IntegrationUtils {

	public static void extractListening(final ServerStart serverStart, final Synchronize synchronize) {
		new Thread(() -> {
			synchronize.goOn();
			try {
				serverStart.acceptAllNextClients();
			} catch (final ClientConnectionFailedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public static void extractListening(ServerStart serverStart) {
		Synchronize synchronize = Synchronize.create();
		extractListening(serverStart, synchronize);
		try {
			synchronize.synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
