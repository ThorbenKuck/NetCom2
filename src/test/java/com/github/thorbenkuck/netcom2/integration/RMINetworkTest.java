package com.github.thorbenkuck.netcom2.integration;

import com.github.thorbenkuck.netcom2.annotations.Testing;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.client.*;
import com.github.thorbenkuck.netcom2.network.server.RemoteObjectRegistration;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.clients.JavaDeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.clients.JavaSerializationAdapter;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Ignore
@Testing({ServerStart.class, ClientStart.class
		, RemoteObjectFactory.class, RemoteObjectRegistration.class
		, JavaRemoteInformationInvocationHandler.class, InvocationHandlerProducer.class
		, Connection.class, Synchronize.class
		, JavaSerializationAdapter.class, JavaDeSerializationAdapter.class})
public class RMINetworkTest extends IntegrationTest {

	static {
		testType = RMINetworkTest.class;
	}

	@Test
	public void testPrimitive() throws Exception {
		printRaw("-- Starting to Test RMINetwork with primitive data types (int)");
		print("Creating Server");
		ServerStart serverStart = ServerStart.at(4544);
		serverStart.remoteObjects()
				.register(new ServerRemoteTestInterface(), PrimitiveRemoteTestInterface.class);
		print("Creating Client");
		ClientStart clientStart = ClientStart.at("localhost", 4544);

		print("Launching Server");
		serverStart.launch();
		print("Initializing new Thread");
		IntegrationUtils.extractListening(serverStart);
		print("New Thread running, launching client");
		clientStart.launch();


		print("Requesting RemoteObject from local Server");
		PrimitiveRemoteTestInterface toTest = clientStart.getRemoteObject(PrimitiveRemoteTestInterface.class);
		print("Testing ..");
		assertEquals(11, toTest.increment(10));
		printRaw("-- Test RMINetwork finished successfully");
	}

	@Test
	public void test() throws Exception {
		printRaw("-- Starting to Test RMINetwork");
		print("Creating Server");
		ServerStart serverStart = ServerStart.at(4540);
		serverStart.remoteObjects()
				.register(new ServerRemoteTestInterface(), RemoteTestInterface.class);
		print("Creating Client");
		ClientStart clientStart = ClientStart.at("localhost", 4540);

		print("Launching Server");
		serverStart.launch();
		print("Initializing new Thread");
		IntegrationUtils.extractListening(serverStart);
		print("New Thread running, launching client");
		clientStart.launch();


		print("Requesting RemoteObject from local Server");
		RemoteTestInterface toTest = clientStart.getRemoteObject(RemoteTestInterface.class);
		print("Testing ..");
		assertEquals("invalid", toTest.convert("Am i valid?"));
		assertEquals("valid", toTest.convert("I am valid"));
		printRaw("-- Test RMINetwork finished successfully");
	}

	private final class ServerRemoteTestInterface implements RemoteTestInterface, PrimitiveRemoteTestInterface {
		@Override
		public String convert(String raw) {
			return raw.contains("?") ? "invalid" : "valid";
		}

		@Override
		public int increment(int base) {
			return ++base;
		}
	}

	private interface RemoteTestInterface {
		String convert(String raw);
	}

	private interface PrimitiveRemoteTestInterface {
		int increment(int base);
	}
}
