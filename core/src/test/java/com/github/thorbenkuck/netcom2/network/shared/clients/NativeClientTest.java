package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.TestUtils;
import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.DefaultConnection;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Testing(NativeClient.class)
public class NativeClientTest {

	private CommunicationRegistration registration;

	@Before
	public void before() {
		registration = mock(CommunicationRegistration.class);
	}

	@BeforeClass
	public static void beforeAny() {
		NetComLogging.setLogging(Logging.disabled());
	}

	@Test
	public void disconnect() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		Connection mockedConnection = mock(Connection.class);
		client.setConnection(TestConnectionKey.class, mockedConnection);
		Session session = client.getSession();

		// Act
		client.disconnect();

		// Assert
		verify(mockedConnection).close();
		assertFalse(client.getConnection(TestConnectionKey.class).isPresent());
		assertEquals(session, client.getSession());
		assertTrue(ClientID.isEmpty(client.getID()));
	}

	@Test
	public void getSession() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);

		// Act
		Session session = client.getSession();

		// Assert
		assertNull(session);
	}

	@Test
	public void setSession() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		Session prev = client.getSession();

		// Act
		client.setSession(mock(Session.class));

		// Assert
		assertNotEquals(prev, client.getSession());
	}

	@Test(expected = IllegalArgumentException.class)
	public void addDisconnectedHandlerNull() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);

		// Act
		client.addDisconnectedHandler(null);

		// Assert
		fail();
	}

	@Test(expected = SendFailedException.class)
	public void send() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		Connection connection = mock(Connection.class);
		when(connection.isOpen()).thenReturn(true);
		when(connection.isConnected()).thenReturn(true);
		client.setConnection(DefaultConnection.class, connection);
		TestObject object = new TestObject();

		// Act
		client.send(object);

		// Assert
		verify(connection).write(any(String.class));
	}

	@Test
	@Ignore
	// TODO Change to current signature
	public void send1() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		Connection connection = mock(Connection.class);
		when(connection.isOpen()).thenReturn(true);
		when(connection.isConnected()).thenReturn(true);
		client.setConnection(TestConnectionKey.class, connection);
		TestObject object = new TestObject();

		// Act
		client.send(object, TestConnectionKey.class);

		// Assert
		verify(connection).write(any(String.class));
	}

	@Test
	@Ignore
	// TODO Change to current signature
	public void send2() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		Connection connection = mock(Connection.class);
		when(connection.isOpen()).thenReturn(true);
		when(connection.isConnected()).thenReturn(true);
		TestObject object = new TestObject();

		// Act
		client.send(object, connection);

		// Assert
		verify(connection).write(any(String.class));
	}

	@Test
	public void getConnectionNotSet() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);

		// Act
		Optional<Connection> optional = client.getConnection(TestConnectionKey.class);

		// Assert
		assertFalse(optional.isPresent());
	}

	@Test
	public void getConnection() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		Connection connection = mock(Connection.class);
		client.setConnection(TestConnectionKey.class, connection);

		// Act
		Optional<Connection> optional = client.getConnection(TestConnectionKey.class);

		// Assert
		assertTrue(optional.isPresent());
		assertEquals(connection, optional.get());
	}

	@Test
	@Ignore
	// TODO Adjust to new signature
	public void createNewConnection() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		Connection connection = mock(Connection.class);
		when(connection.isOpen()).thenReturn(true);
		when(connection.isConnected()).thenReturn(true);
		client.setConnection(DefaultConnection.class, connection);

		// Act
		client.createNewConnection(TestConnectionKey.class);

		// Assert
		verify(connection).write(any(byte[].class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void createNewConnectionNull() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);

		// Act
		client.createNewConnection(null);

		// Assert
		fail();
	}

	@Test
	public void getID() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);

		// Act
		ClientID id = client.getID();

		// Assert
		assertTrue(ClientID.isEmpty(id));
	}

	@Test
	public void setID() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		ClientID id = client.getID();

		// Act
		client.setID(ClientID.fromString(TestUtils.UUID_SEED_1));

		// Assert
		assertEquals(id, client.getID());
	}

	@Test
	public void setConnection() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		Connection connection = mock(Connection.class);

		// Act
		client.setConnection(TestConnectionKey.class, connection);

		// Assert
		Optional<Connection> optional = client.getConnection(TestConnectionKey.class);
		assertTrue(optional.isPresent());
		assertSame(connection, optional.get());
	}

	@Test
	public void routeConnection() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		Connection connection = mock(Connection.class);
		client.setConnection(DefaultConnection.class, connection);

		// Act
		client.routeConnection(DefaultConnection.class, TestConnectionKey.class);

		// Assert
		Optional<Connection> optional = client.getConnection(TestConnectionKey.class);
		assertTrue(optional.isPresent());
		assertSame(connection, optional.get());
	}

	@Test
	public void routeNullConnection() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		Connection connection = mock(Connection.class);
		client.setConnection(DefaultConnection.class, connection);

		// Act
		client.routeConnection(DefaultConnection.class, null);

		// Assert
		Optional<Connection> optional = client.getConnection(null);
		assertTrue(optional.isPresent());
		assertSame(connection, optional.get());
	}

	@Test
	public void routeConnection1() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		Connection connection = mock(Connection.class);
		when(connection.getIdentifier()).thenReturn(Optional.of(DefaultConnection.class));
		client.setConnection(DefaultConnection.class, connection);

		// Act
		client.routeConnection(connection, TestConnectionKey.class);

		// Assert
		Optional<Connection> optional = client.getConnection(TestConnectionKey.class);
		assertTrue(optional.isPresent());
		assertSame(connection, optional.get());
	}

	@Test
	public void routeNullConnection1() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		Connection connection = mock(Connection.class);
		when(connection.getIdentifier()).thenReturn(Optional.of(DefaultConnection.class));
		client.setConnection(DefaultConnection.class, connection);

		// Act
		client.routeConnection(connection, null);

		// Assert
		Optional<Connection> optional = client.getConnection(null);
		assertTrue(optional.isPresent());
		assertSame(connection, optional.get());
	}

	@Test
	public void getCommunicationRegistration() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);

		// Act
		CommunicationRegistration communicationRegistration = client.getCommunicationRegistration();

		// Assert
		assertNotNull(communicationRegistration);
	}

	@Test
	public void prepareConnection() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);

		// Act
		Awaiting awaiting = client.prepareConnection(TestConnectionKey.class);

		// Assert
		assertNotNull(awaiting);
		assertFalse(Synchronize.isEmpty(awaiting));
	}

	@Test
	public void prepareConnectionDouble() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);
		Awaiting awaiting = client.prepareConnection(TestConnectionKey.class);

		// Act
		Awaiting awaitingTwo = client.prepareConnection(TestConnectionKey.class);

		// Assert
		assertSame(awaiting, awaitingTwo);
	}

	@Test(expected = IllegalArgumentException.class)
	public void prepareConnectionNull() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);

		// Act
		client.prepareConnection(null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void notifyAboutPreparedConnectionNotPrepared() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);

		// Act
		client.connectionPrepared(TestConnectionKey.class);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void notifyAboutPreparedConnectionNull() throws Exception {
		// Arrange
		NativeClient client = new NativeClient(registration);

		// Act
		client.connectionPrepared(null);

		// Assert
		fail();
	}

	private static class TestConnectionKey {
	}

	private static class TestObject {
	}

}