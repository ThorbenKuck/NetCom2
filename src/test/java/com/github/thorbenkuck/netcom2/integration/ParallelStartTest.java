package com.github.thorbenkuck.netcom2.integration;

import com.github.thorbenkuck.netcom2.annotations.Testing;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

@Ignore
@Testing({ServerStart.class, ClientStart.class, ReceivePipeline.class, Connection.class, Synchronize.class})
public class ParallelStartTest extends IntegrationTest {

	static {
		testType = ParallelStartTest.class;
	}

	@Test
	public void test() throws Exception {
		printRaw("-- Starting to Test ParallelStart");
		print("Creating dependencies");
		final AtomicReference<Exception> encountered = new AtomicReference<>();
		final Synchronize server = Synchronize.create();
		final Synchronize client = Synchronize.create();
		Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
			if (exception instanceof Exception) {
				encountered.set((Exception) exception);
			}
		});

		print("Initializing ServerStart");
		new Thread(() -> {
			try {
				startServer(server);
			} catch (Exception e) {
				synchronized (encountered) {
					encountered.set(e);
				}
			}
		}).start();

		print("Initializing ClientStart");
		new Thread(() -> {
			try {
				startClient(client);
			} catch (Exception e) {
				synchronized (encountered) {
					encountered.set(e);
				}
			}
		}).start();

		server.synchronize();
		print("Server finished");
		client.synchronize();
		print("Client finished");

		print("Checking thrown Exception ..");
		if (encountered.get() != null) {
			throw encountered.get();
		}
		printRaw("-- Test ParallelStart finished successfully");
	}

	private void startServer(Synchronize synchronize) throws Exception {
		print(" # Creating server ..");
		ServerStart serverStart = ServerStart.at(4543);
		print(" # Starting server ..");
		serverStart.launch();
		print(" # Accepting Clients ..");
		IntegrationUtils.extractListening(serverStart);
		print(" # Server Thread is done");
		synchronize.goOn();
	}

	private void startClient(Synchronize synchronize) throws Exception {
		print(" # Creating Client ..");
		ClientStart clientStart = ClientStart.at("localhost", 4543);
		print(" # Starting client ..");
		clientStart.launch();
		print(" # Client Thread finished");
		synchronize.goOn();
	}

}
