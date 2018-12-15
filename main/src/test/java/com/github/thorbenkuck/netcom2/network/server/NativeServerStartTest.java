package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.exceptions.UnknownClientException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Testing(NativeServerStart.class)
public class NativeServerStartTest {

	private static final InetSocketAddress socketAddress = new InetSocketAddress(8888);
	private NativeServerStart serverStart;
	private ConnectorCore connectorCore;
	private Logging logging;

	@Before
	public void setUp() {
		serverStart = new NativeServerStart(socketAddress);
		logging = mock(Logging.class);
		connectorCore = mock(ConnectorCore.class);
		serverStart.setConnectorCore(connectorCore);
		NetComLogging.setLogging(logging);
	}

	@Test
	public void launch() throws Exception {
		// Arrange

		// Act
		serverStart.launch();

		// Assert
		assertTrue(serverStart.running());
		verify(connectorCore).establishConnection(any());
		verify(connectorCore, never()).disconnect();
	}

	@Test(expected = StartFailedException.class)
	public void launchFail() throws Exception {
		// Arrange
		doThrow(StartFailedException.class).when(connectorCore).establishConnection(socketAddress);

		// Act
		serverStart.launch();

		// Assert
		fail();
	}

	@Test(expected = ClientConnectionFailedException.class)
	public void acceptAllNextClients() throws Exception {
		// Arrange
		doThrow(ClientConnectionFailedException.class).when(connectorCore).handleNext();
		serverStart.launch();

		// Act
		serverStart.acceptAllNextClients();

		// Assert
		fail();
	}

	@Test
	public void acceptAllNextClientsNotLaunched() throws Exception {
		// Arrange

		// Act
		serverStart.acceptAllNextClients();

		// Assert
		verify(logging).trace(any(String.class));
	}

	@Test
	public void setPort() throws Exception {
		// Arrange

		// Act
		serverStart.setPort(1);

		// Assert
		assertEquals(1, serverStart.getPort());
	}

	@Test(expected = ClientConnectionFailedException.class)
	public void acceptNextClient() throws Exception {
		// Arrange
		doThrow(ClientConnectionFailedException.class).when(connectorCore).handleNext();
		serverStart.launch();

		// Act
		serverStart.acceptNextClient();

		// Assert
		fail();
	}

	@Test(expected = ClientConnectionFailedException.class)
	public void acceptNextClientNotLaunched() throws Exception {
		// Arrange
		doThrow(ClientConnectionFailedException.class).when(connectorCore).handleNext();

		// Act
		serverStart.acceptNextClient();

		// Assert
		fail();
	}

	@Test
	public void cache() throws Exception {
		// Arrange

		// Act
		Cache cache = serverStart.cache();

		// Assert
		assertNotNull(cache);
	}

	@Test
	public void cacheAfterLaunch() throws Exception {
		// Arrange
		serverStart.launch();

		// Act
		Cache cache = serverStart.cache();

		// Assert
		assertNotNull(cache);
	}

	@Test
	public void disconnectWithoutLaunch() throws Exception {
		// Arrange

		// Act
		serverStart.disconnect();

		// Assert
		assertFalse(serverStart.running());
	}

	@Test
	public void disconnect() throws Exception {
		// Arrange
		serverStart.launch();

		// Act
		serverStart.disconnect();

		// Assert
		assertFalse(serverStart.running());
	}

	@Test
	public void clientList() throws Exception {
		// Arrange
		serverStart.launch();

		// Act
		ClientList clients = serverStart.clientList();

		// Assert
		assertNotNull(clients);
	}

	@Test
	public void clientListNotLaunched() throws Exception {
		// Arrange

		// Act
		ClientList clients = serverStart.clientList();

		// Assert
		assertNotNull(clients);
	}

	@Test
	public void getCommunicationRegistration() throws Exception {
		// Arrange
		serverStart.launch();

		// Act
		CommunicationRegistration registration = serverStart.getCommunicationRegistration();

		// Assert
		assertNotNull(registration);
	}

	@Test
	public void getCommunicationRegistrationNotRunning() throws Exception {
		// Arrange

		// Act
		CommunicationRegistration registration = serverStart.getCommunicationRegistration();

		// Assert
		assertNotNull(registration);
	}

	@Test
	public void softStop() throws Exception {
		// Arrange

		serverStart.launch();

		// Act
		serverStart.softStop();

		// Assert
		assertFalse(serverStart.running());
	}

	@Test
	public void softStopNotRunning() throws Exception {
		// Arrange


		// Act
		serverStart.softStop();

		// Assert
		assertFalse(serverStart.running());
	}

	@Test
	public void running() throws Exception {
		// Arrange

		serverStart.launch();

		// Act
		boolean running = serverStart.running();

		// Assert
		assertTrue(running);
	}

	@Test
	public void runningNotLaunched() throws Exception {
		// Arrange


		// Act
		boolean running = serverStart.running();

		// Assert
		assertFalse(running);
	}

	@Test(expected = UnknownClientException.class)
	public void createNewConnectionWithoutClient() throws Exception {
		// Arrange

		// Act
		Awaiting awaiting = serverStart.createNewConnection(mock(Session.class), TestConnectionKey.class);

		// Assert
		fail();
	}

	@Test
	public void createNewConnection() throws Exception {
		// Arrange

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