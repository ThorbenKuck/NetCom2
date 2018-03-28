package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.Testing;
import com.github.thorbenkuck.netcom2.exceptions.UnRegistrationException;
import com.github.thorbenkuck.netcom2.network.shared.cache.AbstractCacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Testing(SenderImpl.class)
public class SenderImplTest {
	@Test
	public void objectToServer() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		TestSendObject testSendObject = new TestSendObject();

		// Act
		sender.objectToServer(testSendObject);

		// Assert
		verify(client).send(eq(testSendObject));
	}

	@Test
	public void objectToServer1() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		TestSendObject testSendObject = new TestSendObject();

		// Act
		sender.objectToServer(testSendObject, TestConnection.class);

		// Assert
		verify(client).send(eq(TestConnection.class), eq(testSendObject));
	}

	@Test
	public void objectToServer2() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		Connection connection = mock(Connection.class);
		SenderImpl sender = new SenderImpl(client);
		TestSendObject testSendObject = new TestSendObject();

		// Act
		sender.objectToServer(testSendObject, connection);

		// Assert
		verify(client).send(eq(connection), eq(testSendObject));
	}

	@Test
	public void registrationToServer() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.registrationToServer(TestSendObject.class, observer);

		// Assert
		verify(client).send(any(RegisterRequest.class));
		assertEquals(observer, sender.getPendingObserver(TestSendObject.class));
	}

	@Test
	public void registrationToServer1() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.registrationToServer(TestSendObject.class, observer, TestConnection.class);

		// Assert
		verify(client).send(eq(TestConnection.class), any(RegisterRequest.class));
		assertEquals(observer, sender.getPendingObserver(TestSendObject.class));
	}

	@Test
	public void registrationToServer2() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		Connection connection = mock(Connection.class);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.registrationToServer(TestSendObject.class, observer, connection);

		// Assert
		verify(client).send(eq(connection), any(RegisterRequest.class));
		assertEquals(observer, sender.getPendingObserver(TestSendObject.class));
	}

	@Test
	public void unRegistrationToServer() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.addPendingObserver(TestSendObject.class, observer);
		sender.unRegistrationToServer(TestSendObject.class);

		// Assert
		verify(client).send(any(UnRegisterRequest.class));
		assertEquals(observer, sender.getPendingObserver(TestSendObject.class));
	}

	@Test(expected = UnRegistrationException.class)
	public void unRegistrationToServerNeg() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);

		// Act
		sender.unRegistrationToServer(TestSendObject.class);

		// Also valid, but currently not the case
		// Assert
		verify(client, never()).send(any());
		assertNull(sender.getPendingObserver(TestSendObject.class));
	}

	@Test
	public void unRegistrationToServer1() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.addPendingObserver(TestSendObject.class, observer);
		sender.unRegistrationToServer(TestSendObject.class, TestConnection.class);

		// Assert
		verify(client).send(eq(TestConnection.class), any(UnRegisterRequest.class));
		assertEquals(observer, sender.getPendingObserver(TestSendObject.class));
	}

	@Test(expected = UnRegistrationException.class)
	public void unRegistrationToServer1Neg() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		// Act
		sender.unRegistrationToServer(TestSendObject.class, TestConnection.class);

		// Also valid, but currently not the case
		// Assert
		verify(client, never()).send(eq(TestConnection.class), any());
		assertNull(sender.getPendingObserver(TestSendObject.class));
	}

	@Test
	public void unRegistrationToServer2() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		Connection connection = mock(Connection.class);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.addPendingObserver(TestSendObject.class, observer);
		sender.unRegistrationToServer(TestSendObject.class, connection);

		// Assert
		verify(client).send(eq(connection), any(UnRegisterRequest.class));
		assertEquals(observer, sender.getPendingObserver(TestSendObject.class));
	}

	@Test(expected = UnRegistrationException.class)
	public void unRegistrationToServer2Neg() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		Connection connection = mock(Connection.class);

		// Act
		sender.unRegistrationToServer(TestSendObject.class, connection);

		// Also valid, but currently not the case
		// Assert
		verify(client).send(eq(connection), any(UnRegisterRequest.class));
		assertNull(sender.getPendingObserver(TestSendObject.class));
	}

	@Test
	public void reset() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.addPendingObserver(TestSendObject.class, observer);
		sender.reset();

		// Assert
		assertNull(sender.getPendingObserver(TestSendObject.class));
	}

	@Test
	public void addPendingObserver() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.addPendingObserver(TestSendObject.class, observer);

		// Assert
		assertEquals(observer, sender.getPendingObserver(TestSendObject.class));
	}

	@Test
	public void removePendingObserver() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.addPendingObserver(TestSendObject.class, observer);
		sender.removePendingObserver(TestSendObject.class);

		// Assert
		assertNull(sender.getPendingObserver(TestSendObject.class));
	}

	@Test
	public void getPendingObserver() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		SenderImpl sender = new SenderImpl(client);
		TestCacheObserver<TestSendObject> observer = new TestCacheObserver<>(TestSendObject.class);

		// Act
		sender.addPendingObserver(TestSendObject.class, observer);

		// Assert
		assertEquals(observer, sender.getPendingObserver(TestSendObject.class));
		sender.removePendingObserver(TestSendObject.class);
		assertNull(sender.getPendingObserver(TestSendObject.class));
	}

	private class TestSendObject {
	}

	private class TestConnection {
	}

	private static class TestCacheObserver<T> extends AbstractCacheObserver<T> {
		TestCacheObserver(final Class<T> clazz) {
			super(clazz);
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
	}
}