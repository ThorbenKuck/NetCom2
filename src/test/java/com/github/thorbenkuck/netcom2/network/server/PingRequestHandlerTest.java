package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.TestUtils;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PingRequestHandlerTest {

	private ClientList clients;

	@Before
	public void before() {
		clients = mock(ClientList.class);
	}

	@Test
	public void acceptClientDoesExistSameSession() throws Exception {
		// Arrange
		PingRequestHandler handler = new PingRequestHandler(clients);
		Connection connection = mock(Connection.class);
		Session session = mock(Session.class);
		Ping ping = new Ping(ClientID.fromString(TestUtils.UUID_SEED_1));
		Client client = mock(Client.class);
		when(client.getSession()).thenReturn(session);
		when(clients.getClient(session)).thenReturn(Optional.of(client));

		// Act
		handler.accept(connection, session, ping);

		// Assert
		verify(client).triggerPrimation();
	}

	@Test
	public void acceptClientDoesExistWrongSession() throws Exception {
		// Arrange
		Logging logging = mock(Logging.class);
		NetComLogging.setLogging(logging);
		PingRequestHandler handler = new PingRequestHandler(clients);
		Connection connection = mock(Connection.class);
		Session session = mock(Session.class);
		Ping ping = new Ping(ClientID.fromString(TestUtils.UUID_SEED_1));
		Client client = mock(Client.class);
		when(client.getSession()).thenReturn(mock(Session.class));
		when(clients.getClient(session)).thenReturn(Optional.of(client));

		// Act
		handler.accept(connection, session, ping);

		// Assert
		verify(logging).warn(contains("[ATTENTION] Detected malicious activity at"));
		verify(logging).warn(eq("Forcing Disconnect NOW!"));
		verify(client).disconnect();
	}

	@Test
	public void acceptClientDoesNotExist() throws Exception {
		// Arrange
		Logging logging = mock(Logging.class);
		NetComLogging.setLogging(logging);
		PingRequestHandler handler = new PingRequestHandler(clients);
		Connection connection = mock(Connection.class);
		Session session = mock(Session.class);
		Ping ping = new Ping(ClientID.fromString(TestUtils.UUID_SEED_1));
		when(clients.getClient(session)).thenReturn(Optional.empty());

		// Act
		handler.accept(connection, session, ping);

		// Assert
		verify(logging).warn(contains("Could not locate Client for Session"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptNullConnection() throws Exception {
		// Arrange
		ClientList clients = mock(ClientList.class);
		PingRequestHandler handler = new PingRequestHandler(clients);
		Session session = mock(Session.class);
		Ping ping = new Ping(ClientID.fromString(TestUtils.UUID_SEED_1));

		// Act
		handler.accept(null, session, ping);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptNullSession() throws Exception {
		// Arrange
		ClientList clients = mock(ClientList.class);
		PingRequestHandler handler = new PingRequestHandler(clients);
		Connection connection = mock(Connection.class);
		Ping ping = new Ping(ClientID.fromString(TestUtils.UUID_SEED_1));

		// Act
		handler.accept(connection, null, ping);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptNullPing() throws Exception {
		// Arrange
		ClientList clients = mock(ClientList.class);
		PingRequestHandler handler = new PingRequestHandler(clients);
		Connection connection = mock(Connection.class);
		Session session = mock(Session.class);

		// Act
		handler.accept(connection, session, null);

		// Assert
		fail();
	}

}