package com.github.thorbenkuck.netcom2.integration;

import com.github.thorbenkuck.netcom2.annotations.Testing;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.clients.JavaDeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.clients.JavaSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@Ignore
@Testing({ServerStart.class, ClientStart.class
		, CommunicationRegistration.class, ReceivePipeline.class
		, Connection.class, Synchronize.class, Sender.class
		, JavaSerializationAdapter.class, JavaDeSerializationAdapter.class})
public class DefaultClientServerNetworkTest extends IntegrationTest {

	static {
		testType = DefaultClientServerNetworkTest.class;
	}

	@Test
	public void test() throws Exception {
		printRaw("-- Starting to Test Communication");
		print("Instantiating dependencies");
		Synchronize finished = Synchronize.create();
		ServerStart serverStart = ServerStart.at(4541);
		ClientStart clientStart = ClientStart.at("localhost", 4541);
		AtomicBoolean success = new AtomicBoolean(false);

		print("Registering Test-Communication protocols");
		serverStart.getCommunicationRegistration()
				.register(IntegrationTestObject.class)
				.addFirst((session, integrationTestObject) -> {
					print("Local test server received the tested object");
					session.send(new IntegrationTestObject("Server: " + integrationTestObject.getValue()));
				});

		clientStart.getCommunicationRegistration()
				.register(IntegrationTestObject.class)
				.addFirst(integrationTestObject -> {
					print("Local test client received the potential Test result. Storing information ..");
					success.set(integrationTestObject.getValue().startsWith("Server:"));
					finished.goOn();
				});

		serverStart.launch();
		print("initializing new Thread");
		IntegrationUtils.extractListening(serverStart);
		clientStart.launch();

		print("All components ready. Starting Test");
		clientStart.send()
				.objectToServer(new IntegrationTestObject("Client: Hi!"));
		print("awaiting finish");
		finished.synchronize();
		assertTrue(success.get());
		printRaw("-- Test Communication finished successfully\n");
	}

	@Test
	public void testDefaultCommunication() throws Exception {
		printRaw("-- Starting to Test DefaultCommunication");
		print("Instantiating dependencies");
		Synchronize finished = Synchronize.create();
		ServerStart serverStart = ServerStart.at(4542);
		ClientStart clientStart = ClientStart.at("localhost", 4542);
		IntegrationTestObject testObject = new IntegrationTestObject("NOT_TO_BE_RETURNED");
		AtomicReference<Object> success = new AtomicReference<>();

		print("Registering Test-Communication protocols");
		serverStart.getCommunicationRegistration()
				.addDefaultCommunicationHandler(object -> {
					success.set(object);
					finished.goOn();
				});

		serverStart.launch();
		print("initializing new Thread");
		IntegrationUtils.extractListening(serverStart);
		clientStart.launch();

		print("All components ready. Starting Test");
		clientStart.send()
				.objectToServer(testObject);
		print("awaiting finish");
		finished.synchronize();
		assertEquals(testObject, success.get());
		printRaw("-- Test DefaultCommunication successfully finished\n");
	}
}