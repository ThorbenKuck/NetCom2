package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.TestUtils;
import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.network.interfaces.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.clients.JavaSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Testing(ClientImpl.class)
public class ClientImplTest {

	private CommunicationRegistration registration;

	@Before
	public void before() {
		registration = mock(CommunicationRegistration.class);
	}

	@Test
	public void setThreadPool() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		Connection mockedConnection = mock(Connection.class);
		client.setConnection(TestConnectionKey.class, mockedConnection);
		ExecutorService threadPool = Executors.newCachedThreadPool();

		// Act
		client.setThreadPool(threadPool);

		// Assert
		verify(mockedConnection).setThreadPool(eq(threadPool));
	}

	@Test
	public void setupMethod() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		Session session = client.getSession();

		// Act
		client.setup();

		// Assert
		assertNotEquals(session, client.getSession());
	}

	@Test
	public void disconnect() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		Connection mockedConnection = mock(Connection.class);
		client.setConnection(TestConnectionKey.class, mockedConnection);
		Session session = client.getSession();

		// Act
		client.disconnect();

		// Assert
		verify(mockedConnection).close();
		assertFalse(client.getConnection(TestConnectionKey.class).isPresent());
		assertNotEquals(session, client.getSession());
		assertTrue(ClientID.isEmpty(client.getID()));
	}

	@Test
	public void getSession() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		Session session = client.getSession();

		// Assert
		assertNotNull(session);
	}

	@Test
	public void setSession() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		Session prev = client.getSession();

		// Act
		client.setSession(mock(Session.class));

		// Assert
		assertNotEquals(prev, client.getSession());
	}

	@Test
	public void clearSession() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.clearSession();

		// Assert
		assertNull(client.getSession());
	}

	@Test(expected = IllegalArgumentException.class)
	public void addDisconnectedHandlerNull() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.addDisconnectedHandler(null);

		// Assert
		fail();
	}

	@Test
	public void send() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		Connection connection = mock(Connection.class);
		when(connection.isActive()).thenReturn(true);
		client.setConnection(DefaultConnection.class, connection);
		TestObject object = new TestObject();

		// Act
		client.send(object);

		// Assert
		verify(connection).write(eq(object));
	}

	@Test
	public void send1() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		Connection connection = mock(Connection.class);
		when(connection.isActive()).thenReturn(true);
		client.setConnection(TestConnectionKey.class, connection);
		TestObject object = new TestObject();

		// Act
		client.send(TestConnectionKey.class, object);

		// Assert
		verify(connection).write(eq(object));
	}

	@Test
	public void send2() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		Connection connection = mock(Connection.class);
		when(connection.isActive()).thenReturn(true);
		TestObject object = new TestObject();

		// Act
		client.send(connection, object);

		// Assert
		verify(connection).write(eq(object));
	}

	@Test
	public void getConnectionNotSet() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		Optional<Connection> optional = client.getConnection(TestConnectionKey.class);

		// Assert
		assertFalse(optional.isPresent());
	}

	@Test
	public void getConnection() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		Connection connection = mock(Connection.class);
		client.setConnection(TestConnectionKey.class, connection);

		// Act
		Optional<Connection> optional = client.getConnection(TestConnectionKey.class);

		// Assert
		assertTrue(optional.isPresent());
		assertEquals(connection, optional.get());
	}

	@Test
	public void createNewConnection() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		Connection connection = mock(Connection.class);
		when(connection.isActive()).thenReturn(true);
		client.setConnection(DefaultConnection.class, connection);

		// Act
		client.createNewConnection(TestConnectionKey.class);

		// Assert
		verify(connection).write(any(NewConnectionRequest.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void createNewConnectionNull() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.createNewConnection(null);

		// Assert
		fail();
	}

	@Test
	public void getAnyConnection() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		Connection connection = mock(Connection.class);
		client.setConnection(DefaultConnection.class, connection);

		// Act
		Connection randomConnection = client.getAnyConnection();

		// Assert
		assertNotNull(randomConnection);
		assertEquals(connection, randomConnection);
	}

	@Test
	public void getAnyConnectionTwoSet() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		Connection connection = mock(Connection.class);
		Connection connectionTwo = mock(Connection.class);
		client.setConnection(DefaultConnection.class, connection);
		client.setConnection(TestConnectionKey.class, connectionTwo);

		// Act
		Connection randomConnection = client.getAnyConnection();

		// Assert
		assertNotNull(randomConnection);
		assertTrue(Arrays.asList(connection, connectionTwo).contains(randomConnection));
	}

	@Test
	public void getAnyConnectionNonSet() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		Connection connection = client.getAnyConnection();

		// Assert
		assertNull(connection);
	}

	@Test
	public void getFormattedAddress() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		String address = "TEST";
		Connection connection = mock(Connection.class);
		when(connection.getFormattedAddress()).thenReturn(address);
		client.setConnection(DefaultConnection.class, connection);

		// Act
		String formattedAddress = client.getFormattedAddress();

		// Assert
		assertSame(address, formattedAddress);
	}

	@Test
	public void getID() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		ClientID id = client.getID();

		// Assert
		assertTrue(ClientID.isEmpty(id));
	}

	@Test
	public void setID() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		ClientID id = client.getID();

		// Act
		client.setID(ClientID.fromString(TestUtils.UUID_SEED_1));

		// Assert
		assertNotEquals(id, client.getID());
	}

	@Test
	public void setConnection() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
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
		ClientImpl client = new ClientImpl(registration);
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
		ClientImpl client = new ClientImpl(registration);
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
		ClientImpl client = new ClientImpl(registration);
		Connection connection = mock(Connection.class);
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
		ClientImpl client = new ClientImpl(registration);
		Connection connection = mock(Connection.class);
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
		ClientImpl client = new ClientImpl(registration);

		// Act
		CommunicationRegistration communicationRegistration = client.getCommunicationRegistration();

		// Assert
		assertNotNull(communicationRegistration);
	}

	@Test
	public void addFallBackSerializationAdapter() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.addFallBackSerializationAdapter(Arrays.asList(Object::toString, Object::toString));

		// Assert
		assertEquals(3, client.getFallBackSerialization().size());
	}

	@Test
	public void addFallBackDeSerializationAdapter() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.addFallBackDeSerializationAdapter(Arrays.asList(Object::toString, Object::toString));

		// Assert
		assertEquals(3, client.getFallBackDeSerialization().size());
	}

	@Test
	public void addFallBackSerialization() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.addFallBackSerialization(Object::toString);

		// Assert
		assertEquals(2, client.getFallBackSerialization().size());
	}

	@Test
	public void addFallBackDeSerialization() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.addFallBackDeSerialization(Object::toString);

		// Assert
		assertEquals(2, client.getFallBackDeSerialization().size());
	}

	/*
	 * This Method ensures, that the JavaSerialization is the main Serialization to ensure, that this is never changed.
	 * If this Tests fails, potentially the MainSerializationAdapter has been changed
	 */
	@Test
	public void getMainSerializationAdapter() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		SerializationAdapter<Object, String> adapter = client.getMainSerializationAdapter();

		// Assert
		assertNotNull(adapter);
		assertEquals(JavaSerializationAdapter.class, adapter.getClass());
	}

	@Test
	public void setMainSerializationAdapter() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		SerializationAdapter<Object, String> adapter = client.getMainSerializationAdapter();

		// Act
		client.setMainSerializationAdapter(Object::toString);

		// Assert
		assertNotEquals(adapter, client.getMainSerializationAdapter());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setMainSerializationAdapterNull() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.setMainSerializationAdapter(null);

		// Assert
		fail();
	}

	/*
	 * This Method ensures, that the JavaSerialization is the main Serialization to ensure, that this is never changed.
	 * If this Tests fails, potentially the MainDeSerializationAdapter has been changed
	 */
	@Test
	public void getMainDeSerializationAdapter() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		DeSerializationAdapter<String, Object> adapter = client.getMainDeSerializationAdapter();

		// Assert
		assertNotNull(adapter);
		assertEquals(JavaDeSerializationAdapter.class, adapter.getClass());
	}

	@Test
	public void setMainDeSerializationAdapter() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		DeSerializationAdapter<String, Object> adapter = client.getMainDeSerializationAdapter();

		// Act
		client.setMainDeSerializationAdapter(Object::toString);

		// Assert
		assertNotEquals(adapter, client.getMainSerializationAdapter());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setMainDeSerializationAdapterNull() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.setMainDeSerializationAdapter(null);

		// Assert
		fail();
	}

	@Test
	public void getFallBackSerialization() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		Set<SerializationAdapter<Object, String>> fallback = client.getFallBackSerialization();

		// Assert
		assertEquals(1, fallback.size());
	}

	@Test
	public void getFallBackDeSerialization() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		Set<DeSerializationAdapter<String, Object>> fallback = client.getFallBackDeSerialization();

		// Assert
		assertEquals(1, fallback.size());
	}

	@Test
	public void getDecryptionAdapter() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		DecryptionAdapter adapter = client.getDecryptionAdapter();

		// Assert
		assertNotNull(adapter);
	}

	@Test
	public void setDecryptionAdapter() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		DecryptionAdapter adapter = client.getDecryptionAdapter();

		// Act
		client.setDecryptionAdapter(string -> string);

		// Assert
		assertNotEquals(adapter, client.getDecryptionAdapter());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setDecryptionAdapterNull() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.setDecryptionAdapter(null);

		// Assert
		fail();
	}

	@Test
	public void getEncryptionAdapter() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		EncryptionAdapter adapter = client.getEncryptionAdapter();

		// Assert
		assertNotNull(adapter);
	}

	@Test
	public void setEncryptionAdapter() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		EncryptionAdapter adapter = client.getEncryptionAdapter();

		// Act
		client.setEncryptionAdapter(string -> string);

		// Assert
		assertNotEquals(adapter, client.getEncryptionAdapter());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setEncryptionAdapterNull() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.setEncryptionAdapter(null);

		// Assert
		fail();
	}

	@Test
	public void prepareConnection() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		Awaiting awaiting = client.prepareConnection(TestConnectionKey.class);

		// Assert
		assertNotNull(awaiting);
		assertFalse(Synchronize.isEmpty(awaiting));
	}

	@Test
	public void prepareConnectionDouble() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		Awaiting awaiting = client.prepareConnection(TestConnectionKey.class);

		// Act
		Awaiting awaitingTwo = client.prepareConnection(TestConnectionKey.class);

		// Assert
		assertSame(awaiting, awaitingTwo);
	}

	@Test(expected = IllegalArgumentException.class)
	public void prepareConnectionNull() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.prepareConnection(null);

		// Assert
		fail();
	}

	@Test
	public void isConnectionPrepared() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		boolean prep = client.isConnectionPrepared(TestConnectionKey.class);

		// Assert
		assertFalse(prep);
	}

	@Test
	public void isConnectionPreparedSuccessful() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		client.prepareConnection(TestConnectionKey.class);

		// Act
		boolean prep = client.isConnectionPrepared(TestConnectionKey.class);

		// Assert
		assertTrue(prep);
	}

	@Test(expected = IllegalArgumentException.class)
	public void notifyAboutPreparedConnectionNotPrepared() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.notifyAboutPreparedConnection(TestConnectionKey.class);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void notifyAboutPreparedConnectionNull() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.notifyAboutPreparedConnection(null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void addFalseIDNull() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.addFalseID(null);

		// Assert
		fail();
	}

	@Test
	public void getFalseIDs() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		List<ClientID> ids = client.getFalseIDs();

		// Assert
		assertTrue(ids.isEmpty());
	}

	@Test
	public void getFalseIDsWithSet() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);
		client.addFalseID(ClientID.fromString(TestUtils.UUID_SEED_1));

		// Act
		List<ClientID> ids = client.getFalseIDs();

		// Assert
		assertFalse(ids.isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void removeFalseIDNull() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.removeFalseID(null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void removeFalseIDs() throws Exception {
		// Arrange
		ClientImpl client = new ClientImpl(registration);

		// Act
		client.removeFalseID(null);

		// Assert
		fail();
	}

	private static class TestConnectionKey {
	}

	private static class TestObject {
	}

}