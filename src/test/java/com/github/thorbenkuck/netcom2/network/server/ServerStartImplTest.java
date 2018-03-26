package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.interfaces.Factory;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServerStartImplTest {

	private ServerConnector connector;
	private Logging logging;

	@Before
	public void setUp() {
		connector = mock(ServerConnector.class);
		logging = mock(Logging.class);
		NetComLogging.setLogging(logging);
	}

	@Test
	public void launch() throws Exception {
		// Arrange
		ServerStartImpl serverStart = new ServerStartImpl(connector);

		// Act
		serverStart.launch();

		// Assert
		assertTrue(serverStart.running());
		verify(connector).establishConnection(any());
		verify(connector, never()).shutDown();
	}

	@Test(expected = StartFailedException.class)
	public void launchFail() throws Exception {
		// Arrange
		when(connector.establishConnection(any())).thenThrow(new IOException());
		ServerStartImpl serverStart = new ServerStartImpl(connector);

		// Act
		serverStart.launch();

		// Assert
		fail();
	}

	@Test(expected = StartFailedException.class)
	public void launchDouble() throws Exception {
		// Arrange
		when(connector.establishConnection(any())).thenThrow(new IOException());
		ServerStartImpl serverStart = new ServerStartImpl(connector);

		// Act
		serverStart.launch();
		assertFalse(serverStart.running());
		verify(connector, never()).establishConnection(any());
		verify(connector).shutDown();
		serverStart.launch();

		// Assert
		fail();
	}

	@Test(expected = ClientConnectionFailedException.class)
	public void acceptAllNextClients() throws Exception {
		// Arrange
		when(connector.getServerSocket()).thenThrow(new IllegalArgumentException());
		ServerStartImpl serverStart = new ServerStartImpl(connector);
		serverStart.launch();

		// Act
		serverStart.acceptAllNextClients();

		// Assert
		fail();
	}

	@Test
	public void acceptAllNextClientsNotLaunched() throws Exception {
		// Arrange
		ServerStartImpl serverStart = new ServerStartImpl(connector);

		// Act
		serverStart.acceptAllNextClients();

		// Assert
		verify(logging).warn(any(String.class));
	}

	@Test
	public void setPort() throws Exception {
		// Arrange
		ServerStartImpl serverStart = new ServerStartImpl(connector);

		// Act
		serverStart.setPort(1);

		// Assert
		assertEquals(1, serverStart.getPort());
	}

	@Test(expected = ClientConnectionFailedException.class)
	public void acceptNextClient() throws Exception {
		// Arrange
		ServerSocket socket = mock(ServerSocket.class);
		when(connector.getServerSocket()).thenReturn(socket);
		when(socket.accept()).thenThrow(new IOException());
		ServerStartImpl serverStart = new ServerStartImpl(connector);
		serverStart.launch();

		// Act
		serverStart.acceptNextClient();

		// Assert
		fail();
	}

	@Test(expected = ClientConnectionFailedException.class)
	public void acceptNextClientNotLaunched() throws Exception {
		// Arrange
		when(connector.getServerSocket()).thenThrow(new IllegalArgumentException());
		ServerStartImpl serverStart = new ServerStartImpl(connector);

		// Act
		serverStart.acceptNextClient();

		// Assert
		verify(logging).warn(any(String.class));
	}

	@Test
	public void distribute() throws Exception {
		// Arrange
		ServerStartImpl serverStart = new ServerStartImpl(connector);

		// Act
		Distributor distributor = serverStart.distribute();

		// Assert
		assertNotNull(distributor);
	}

	@Test
	public void distributeAfterLaunch() throws Exception {
		// Arrange
		ServerStartImpl serverStart = new ServerStartImpl(connector);
		serverStart.launch();

		// Act
		Distributor distributor = serverStart.distribute();

		// Assert
		assertNotNull(distributor);
	}

	@Test
	public void cache() throws Exception {
		// Arrange
		ServerStartImpl serverStart = new ServerStartImpl(connector);

		// Act
		Cache cache = serverStart.cache();

		// Assert
		assertNotNull(cache);
	}

	@Test
	public void cacheAfterLaunch() throws Exception {
		// Arrange
		ServerStartImpl serverStart = new ServerStartImpl(connector);
		serverStart.launch();

		// Act
		Cache cache = serverStart.cache();

		// Assert
		assertNotNull(cache);
	}

	@Test
	public void disconnectWithoutLaunch() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);

		// Act
		serverStart.disconnect();

		// Assert
		assertFalse(serverStart.running());
	}

	@Test
	public void disconnect() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);
		serverStart.launch();

		// Act
		serverStart.disconnect();

		// Assert
		assertFalse(serverStart.running());
	}

	@Test
	public void setServerSocketFactory() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);
		int port = 1234;
		Factory<Integer, ServerSocket> factory = new Factory<Integer, ServerSocket>() {
			@Override
			public ServerSocket create(final Integer integer) {
				assertEquals(port, integer.intValue());
				return mock(ServerSocket.class);
			}
		};

		// Act
		serverStart.setServerSocketFactory(factory);
		serverStart.launch();

		// Assert
		verify(connector).establishConnection(eq(factory));
	}

	@Test
	public void clientList() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);
		serverStart.launch();

		// Act
		ClientList clients = serverStart.clientList();

		// Assert
		assertNotNull(clients);
	}

	@Test
	public void clientListNotLaunched() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);

		// Act
		ClientList clients = serverStart.clientList();

		// Assert
		assertNotNull(clients);
	}

	@Test
	public void getCommunicationRegistration() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);
		serverStart.launch();

		// Act
		CommunicationRegistration registration = serverStart.getCommunicationRegistration();

		// Assert
		assertNotNull(registration);
	}

	@Test
	public void getCommunicationRegistrationNotRunning() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);

		// Act
		CommunicationRegistration registration = serverStart.getCommunicationRegistration();

		// Assert
		assertNotNull(registration);
	}

	@Test
	public void remoteObjects() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);
		serverStart.launch();

		// Act
		RemoteObjectRegistration remoteObjectRegistration = serverStart.remoteObjects();

		// Assert
		assertNotNull(remoteObjectRegistration);
	}

	@Test
	public void remoteObjectsNotLaunched() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);

		// Act
		RemoteObjectRegistration remoteObjectRegistration = serverStart.remoteObjects();

		// Assert
		assertNotNull(remoteObjectRegistration);
	}

	@Test
	public void softStop() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);
		serverStart.launch();

		// Act
		serverStart.softStop();

		// Assert
		assertFalse(serverStart.running());
	}

	@Test
	public void softStopNotRunning() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);

		// Act
		serverStart.softStop();

		// Assert
		assertFalse(serverStart.running());
	}

	@Test
	public void running() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);
		serverStart.launch();

		// Act
		boolean running = serverStart.running();

		// Assert
		assertTrue(running);
	}

	@Test
	public void runningNotLaunched() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);

		// Act
		boolean running = serverStart.running();

		// Assert
		assertFalse(running);
	}

	@Test
	public void createNewConnectionWithoutClient() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);

		// Act
		Awaiting awaiting = serverStart.createNewConnection(mock(Session.class), TestConnectionKey.class);

		// Assert
		assertTrue(Synchronize.isEmpty(awaiting));
	}

	@Test
	public void createNewConnection() throws Exception {
		// Arrange
		ServerStart serverStart = new ServerStartImpl(connector);
		Client client = mock(Client.class);
		Session session = mock(Session.class);
		when(client.getSession()).thenReturn(session);
		serverStart.clientList().add(client);

		// Act
		serverStart.createNewConnection(session, TestConnectionKey.class);

		// Assert
		verify(client).createNewConnection(eq(TestConnectionKey.class));
	}

	private class TestConnectionKey {
	}
}