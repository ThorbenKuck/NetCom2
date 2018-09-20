package com.github.thorbenkuck.netcom2.integration.connections;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.integration.TestObject;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

	private static int receivedCount = 0;

	public static void main(String[] args) {
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleAtFixedRate(Server::diagnose, 1, 1, TimeUnit.SECONDS);
		ServerStart serverStart = ServerStart.at(4569);
		NetComThreadPool.startWorkerProcesses(10);
		NetComLogging.setLogging(Logging.disabled());

		serverStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(Server::printAndPing);

//		serverStart.addClientConnectedHandler(client -> {
//			println("[EXAMPLE_1]: New Connection Handler called");
//			try {
//				println("[EXAMPLE_2]: Awaiting primed client");
//				client.primed().synchronize();
//				println("[EXAMPLE_3]: Client connected, establishing new TestConnection");
//				Awaiting awaiting = client.createNewConnection(TestConnectionKey.class);
//				println("[EXAMPLE_4]: Waiting for the Connection to be established");
//				awaiting.synchronize();
//				println("[EXAMPLE_5]: Connection is established successfully");
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			println("[EXAMPLE_6]: TestConnection established");
//			Connection connection = client.getConnection(TestConnectionKey.class)
//					.orElseThrow(IllegalStateException::new);
//
//			println("[EXAMPLE_7]: Sending TestObject over the new Connection");
//			connection.context()
//					.send(new TestObject("From TestConnection"));
//		});

		try {
			serverStart.launch();
			serverStart.acceptAllNextClients();
		} catch (StartFailedException | ClientConnectionFailedException e) {
			e.printStackTrace();
		}
	}

	private static void diagnose() {
		println(NetComThreadPool.generateDiagnosticOutput());
	}

	private static void println(Object object) {
		System.out.println(object);
	}

	private static synchronized void printAndPing(ConnectionContext connectionContext, Session session, TestObject testObject) {
		println(++receivedCount + "] Received from " + connectionContext.getIdentifier().getSimpleName() + ": " + testObject);
		connectionContext.send(testObject);
	}

}
