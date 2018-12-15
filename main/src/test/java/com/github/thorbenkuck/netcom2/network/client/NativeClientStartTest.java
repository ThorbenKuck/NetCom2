package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.exceptions.ConnectionEstablishmentFailedException;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.DeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.SerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientDisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;

@Testing(NativeClientStart.class)
public class NativeClientStartTest {

	private static final String ADDRESS = "localhost";
	private static final int PORT = 1;
	private static final SocketAddress socketAddress = new InetSocketAddress(ADDRESS, PORT);
	private ClientCore mockedClientCore;

	@Before
	public void beforeEachTest() throws Exception {
		mockedClientCore = mock(ClientCore.class);
	}

	@Test
	public void launch() throws Exception {
		// Arrange
		NativeClientStart clientStart = new NativeClientStart(new InetSocketAddress(ADDRESS, PORT), mockedClientCore);

		// Act
		clientStart.launch();

		// Assert
		assertTrue(clientStart.running());
		verify(mockedClientCore).establishConnection(socketAddress, clientStart.getClient());
	}

	@Test
	public void cache() throws Exception {
		// Arrange
		ClientStart clientStart = new NativeClientStart(socketAddress, mockedClientCore);

		final AtomicBoolean success = new AtomicBoolean(false);
		CacheObserver<TestSendObject> observer = new TestObserver(success);

		// Act
		Cache cache = clientStart.cache();
		cache.addCacheObserver(observer);

		// Assert
		assertEquals(cache, clientStart.cache());
		assertFalse(success.get());
		clientStart.cache().addAndOverride(new TestSendObject());
		assertTrue(success.get());
	}

	@Test
	public void newConnection() throws Exception {
		// Arrange
		NativeClientStart clientStart = new NativeClientStart(socketAddress, mockedClientCore);
		Connection mockedConnection = mock(Connection.class);
		when(mockedConnection.getIdentifier()).thenReturn(Optional.of(TestSendObject.class));
		doAnswer((i) -> {
			clientStart.getClient().addConnection(mockedConnection);
			return null;
		}).when(mockedClientCore).establishConnection(socketAddress, clientStart.getClient(), TestSendObject.class);
		clientStart.launch();

		// Act
		clientStart.newConnection(TestSendObject.class);
		Optional<Connection> connection = clientStart.getClient().getConnection(TestSendObject.class);

		// Assert
		verify(mockedClientCore).establishConnection(socketAddress, clientStart.getClient());
		verify(mockedClientCore).establishConnection(socketAddress, clientStart.getClient(), TestSendObject.class);
		assertTrue(connection.isPresent());
		assertEquals(mockedConnection, connection.get());
	}

	@Test(expected = ConnectionEstablishmentFailedException.class)
	public void newConnectionNeg() throws Exception {
		// Arrange
		ClientStart clientStart = new NativeClientStart(socketAddress, mockedClientCore);

		// Act
		clientStart.newConnection(TestSendObject.class);

		// Assert
		// No assert, assert through expected Exception
		fail();
	}

	@Test(expected = IllegalStateException.class)
	public void setSocketFactory() throws Exception {
		// Arrange
		ClientStart clientStart = new NativeClientStart(socketAddress, mockedClientCore);
		doThrow(IllegalStateException.class).when(mockedClientCore).establishConnection(any(), any());

		// Act
		clientStart.launch();

		// Assert
		// No assert, assert through expected Exception
		fail();
	}

	@Test
	public void addFallBackSerialization() throws Exception {
		// Arrange
		NativeClientStart clientStart = new NativeClientStart(socketAddress, mockedClientCore);
		SerializationAdapter adapter = Object::toString;

		// Act
		clientStart.addFallBackSerialization(adapter);

		// Assert
		// TODO
	}

	@Test
	public void addFallBackDeSerialization() throws Exception {
		// Arrange
		NativeClientStart clientStart = new NativeClientStart(socketAddress, mockedClientCore);
		DeSerializationAdapter adapter = o -> o;

		// Act
		clientStart.addFallBackDeSerialization(adapter);

		// Assert
		// TODO
	}

	@Test
	public void setMainSerializationAdapter() throws Exception {
		// Arrange
		NativeClientStart clientStart = new NativeClientStart(socketAddress, mockedClientCore);
		SerializationAdapter adapter = Object::toString;

		// Act
		clientStart.setMainSerializationAdapter(adapter);

		// Assert
		// TODO
	}

	@Test
	public void setMainDeSerializationAdapter() throws Exception {
		// Arrange
		NativeClientStart clientStart = new NativeClientStart(socketAddress, mockedClientCore);
		DeSerializationAdapter adapter = o -> o;

		// Act
		clientStart.setMainDeSerializationAdapter(adapter);

		// Assert
		// TODO
	}

	@Test
	public void addDisconnectedHandler() throws Exception {
		// Arrange
		NativeClientStart clientStart = new NativeClientStart(socketAddress, mockedClientCore);

		ClientDisconnectedHandler clientDisconnectedHandler = client -> {
		};

		// Act
		clientStart.addDisconnectedHandler(clientDisconnectedHandler);

		// Assert
		// TODO
	}

	@Test
	public void getCommunicationRegistration() throws Exception {
		// Arrange
		ClientStart clientStart = new NativeClientStart(socketAddress, mockedClientCore);

		// Act
		CommunicationRegistration communicationRegistration = clientStart.getCommunicationRegistration();

		// Assert
		assertNotNull(communicationRegistration);
	}

	@Test
	public void getCommunicationRegistration1() throws Exception {
		// Arrange
		NativeClientStart clientStart = new NativeClientStart(socketAddress, mockedClientCore);

		// Act
		CommunicationRegistration communicationRegistration = clientStart.getCommunicationRegistration();
		clientStart.launch();

		// Assert
		assertNotNull(communicationRegistration);
		assertEquals(communicationRegistration, clientStart.getCommunicationRegistration());
	}

	@Test
	public void clearCache() throws Exception {
		// Arrange
		ClientStart clientStart = new NativeClientStart(socketAddress, mockedClientCore);
		final AtomicBoolean success = new AtomicBoolean(false);
		CacheObserver<TestSendObject> observer = new TestObserver(success);

		// Act
		clientStart.cache().addCacheObserver(observer);
		Cache cache = clientStart.cache();
		clientStart.clearCache();

		// Assert
		assertEquals(cache, clientStart.cache());
		assertEquals(0, cache.countObservers());
	}

	private class TestObserver implements CacheObserver<TestSendObject> {

		private final AtomicBoolean success;

		private TestObserver(AtomicBoolean success) {
			this.success = success;
		}

		@Override
		public void newEntry(final TestSendObject testSendObject, final CacheObservable observable) {
			success.set(!success.get());
		}

		@Override
		public void updatedEntry(final TestSendObject testSendObject, final CacheObservable observable) {
			success.set(!success.get());
		}

		@Override
		public void deletedEntry(final TestSendObject testSendObject, final CacheObservable observable) {
			success.set(!success.get());
		}

		@Override
		public boolean accept(Object o) {
			return TestSendObject.class.equals(o.getClass());
		}
	}

	private class TestSendObject {
	}

}