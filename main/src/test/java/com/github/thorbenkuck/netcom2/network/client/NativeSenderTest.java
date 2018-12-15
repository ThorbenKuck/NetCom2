package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.CacheRegistration;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.CacheUnRegistration;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Testing(NativeSender.class)
public class NativeSenderTest {

	private NativeClientStart clientStart;
	private Client client;

	@Before
	public void beforeEachTest() throws InterruptedException {
		clientStart = mock(NativeClientStart.class);
		client = mock(Client.class);
		CommunicationRegistration registration = mock(CommunicationRegistration.class);
		ReceivePipeline mock = mock(ReceivePipeline.class);

		when(clientStart.getClient()).thenReturn(client);
		when(clientStart.getCommunicationRegistration()).thenReturn(registration);
		when(clientStart.cache()).thenReturn(mock(Cache.class));
		doNothing().when(registration).acquire();
		doNothing().when(registration).release();
		when(registration.register(any())).thenReturn(mock);
		doReturn(null).when(mock).addFirst(any(OnReceive.class));
	}

	@Test
	public void objectToServer() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		TestSendObject testSendObject = new TestSendObject();

		// Act
		sender.objectToServer(testSendObject);

		// Assert
		verify(client).send(eq(testSendObject));
	}

	@Test
	public void objectToServer1() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		TestSendObject testSendObject = new TestSendObject();

		// Act
		sender.objectToServer(testSendObject, TestConnection.class);

		// Assert
		verify(client).send(eq(testSendObject), eq(TestConnection.class));
	}

	@Test
	public void objectToServer2() throws Exception {
		// Arrange
		Connection connection = mock(Connection.class);
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		TestSendObject testSendObject = new TestSendObject();

		// Act
		sender.objectToServer(testSendObject, connection);

		// Assert
		verify(client).send(eq(testSendObject), eq(connection));
	}

	@Test
	public void registrationToServer() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.registrationToServer(TestSendObject.class, observer);

		// Assert
		verify(client).send(any(CacheRegistration.class));
	}

	@Test
	public void registrationToServer1() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.registrationToServer(TestSendObject.class, observer, TestConnection.class);

		// Assert
		verify(client).send(any(CacheRegistration.class), eq(TestConnection.class));
	}

	@Test
	public void registrationToServer2() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		Connection connection = mock(Connection.class);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.registrationToServer(TestSendObject.class, observer, connection);

		// Assert
		verify(client).send(any(CacheRegistration.class), eq(connection));
	}

	@Test
	public void unRegistrationToServer() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.registrationToServer(TestSendObject.class, observer);
		sender.unRegistrationToServer(TestSendObject.class);

		// Assert
		verify(client).send(any(CacheUnRegistration.class));
	}

	@Test
	public void unRegistrationToServerNeg() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);

		// Act
		sender.unRegistrationToServer(TestSendObject.class);

		// Also valid, but currently not the case
		// Assert
		verify(client).send(any(CacheUnRegistration.class));
	}

	@Test
	public void unRegistrationToServer1() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.registrationToServer(TestSendObject.class, observer);
		sender.unRegistrationToServer(TestSendObject.class, TestConnection.class);

		// Assert
		verify(client).send(any(CacheUnRegistration.class), eq(TestConnection.class));
	}

	@Test
	public void unRegistrationToServer1Neg() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		// Act
		sender.unRegistrationToServer(TestSendObject.class, TestConnection.class);

		// Also valid, but currently not the case
		// Assert
		verify(client).send(any(CacheUnRegistration.class), eq(TestConnection.class));
	}

	@Test
	public void unRegistrationToServer2() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		Connection connection = mock(Connection.class);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.registrationToServer(TestSendObject.class, observer);
		sender.unRegistrationToServer(TestSendObject.class, connection);

		// Assert
		verify(client).send(any(CacheUnRegistration.class), eq(connection));
	}

	@Test
	public void unRegistrationToServer2Neg() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		Connection connection = mock(Connection.class);

		// Act
		sender.unRegistrationToServer(TestSendObject.class, connection);

		// Also valid, but currently not the case
		// Assert
		verify(client).send(any(CacheUnRegistration.class), eq(connection));
	}

	@Test
	public void reset() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.registrationToServer(TestSendObject.class, observer);
		sender.reset();

		// Assert
		// TODO
	}

	@Test
	public void addPendingObserver() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.registrationToServer(TestSendObject.class, observer);

		// Assert
		// TODO
	}

	@Test
	public void removePendingObserver() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.registrationToServer(TestSendObject.class, observer);
		sender.unRegistrationToServer(TestSendObject.class);

		// Assert
		// TODO
	}

	@Test
	public void getPendingObserver() throws Exception {
		// Arrange
		NativeSender sender = new NativeSender();
		sender.setup(clientStart);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.registrationToServer(TestSendObject.class, observer);

		// Assert
	}

	private class TestSendObject {
	}

	private class TestConnection {
	}

	private static class TestCacheObserver<T> implements CacheObserver<T> {

		private final Class<T> type;

		TestCacheObserver(Class<T> type) {
			this.type = type;
		}

		@Override
		public void newEntry(final T t, final CacheObservable observable) {

		}

		@Override
		public void updatedEntry(final T t, final CacheObservable observable) {

		}

		@Override
		public void deletedEntry(final T t, final CacheObservable observable) {

		}

		@Override
		public boolean accept(Object o) {
			return o != null && type.equals(o.getClass());
		}
	}
}