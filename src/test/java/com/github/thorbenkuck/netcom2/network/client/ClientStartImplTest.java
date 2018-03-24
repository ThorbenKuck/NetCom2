package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.exceptions.*;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.interfaces.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.cache.AbstractCacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.DeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.clients.DefaultConnection;
import com.github.thorbenkuck.netcom2.network.shared.clients.SerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;

public class ClientStartImplTest {

	private final String ADDRESS = "localhost";
	private final int PORT = 1;
	private Socket mockedSocket = mock(Socket.class);
	private InputStream mockedInputStream = mock(InputStream.class);
	private OutputStream mockedOutputStream = mock(OutputStream.class);
	private SocketFactory mockedSocketFactory;

	@Before
	public void beforeEachTest() throws Exception {
		mockedSocketFactory = ((port, address) -> mockedSocket);
		when(mockedSocket.getInputStream()).thenReturn(mockedInputStream);
		when(mockedSocket.getOutputStream()).thenReturn(mockedOutputStream);
	}

	@Test
	public void launch() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);

		// Act
		clientStart.client = mock(Client.class);
		when(clientStart.client.primed()).thenReturn(Synchronize.empty());
		clientStart.launch();

		// Assert
		assertTrue(clientStart.launched().get());
		verify(clientStart.client).primed();
		verify(clientStart.client).getConnection(eq(DefaultConnection.class));
	}

	@Test(expected = StartFailedException.class)
	public void launchNeg() throws Exception {
		// Arrange
		ClientStart clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(((port, address) -> null));

		// Act
		clientStart.launch();

		// Assert
		// Assert via expected Exception
		fail();
	}

	@Test
	public void cache() throws Exception {
		// Arrange
		ClientStart clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		final AtomicBoolean success = new AtomicBoolean(false);
		CacheObserver<TestSendObject> observer = new AbstractCacheObserver<TestSendObject>(TestSendObject.class) {
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
		};

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
	public void createNewConnection() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		clientStart.client = mock(Client.class);
		when(clientStart.client.primed()).thenReturn(Synchronize.empty());
		clientStart.launch();

		// Act
		clientStart.createNewConnection(TestSendObject.class);

		// Assert
		verify(clientStart.client).send(any(NewConnectionRequest.class));
	}

	@Test(expected = IllegalStateException.class)
	public void createNewConnectionNeg() throws Exception {
		// Arrange
		ClientStart clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);

		// Act
		clientStart.createNewConnection(TestSendObject.class);

		// Assert
		// No assert, assert through expected Exception
		fail();
	}

	@Test(expected = IllegalStateException.class)
	public void setSocketFactory() throws Exception {
		// Arrange
		ClientStart clientStart = new ClientStartImpl(ADDRESS, PORT);
		SocketFactory socketFactory = mock(SocketFactory.class);
		when(socketFactory.create(anyInt(), anyString())).thenThrow(new IllegalStateException());
		clientStart.setSocketFactory(socketFactory);

		// Act
		clientStart.launch();

		// Assert
		// No assert, assert through expected Exception
		fail();
	}

	@Test
	public void send() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		clientStart.client = mock(Client.class);
		when(clientStart.client.primed()).thenReturn(Synchronize.empty());
		clientStart.sender.setClient(clientStart.client);
		clientStart.launch();
		TestSendObject testSendObject = new TestSendObject();

		// Act
		clientStart.send().objectToServer(testSendObject);

		// Assert
		verify(clientStart.client).send(testSendObject);
	}

	@Test(expected = SendFailedException.class)
	public void sendNeg() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		TestSendObject testSendObject = new TestSendObject();
		clientStart.client = mock(Client.class);
		when(clientStart.client.send(any())).thenThrow(new SendFailedException(""));

		// Act
		clientStart.send().objectToServer(testSendObject);

		// Assert
		fail();
	}

	@Test
	public void addFallBackSerialization() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		clientStart.client = mock(Client.class);
		SerializationAdapter<Object, String> adapter = new SerializationAdapter<Object, String>() {
			@Override
			public String get(final Object o) throws SerializationFailedException {
				return o.toString();
			}
		};

		// Act
		clientStart.addFallBackSerialization(adapter);

		// Assert
		verify(clientStart.client).addFallBackSerialization(eq(adapter));
	}

	@Test
	public void addFallBackDeSerialization() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		clientStart.client = mock(Client.class);
		DeSerializationAdapter<String, Object> adapter = new DeSerializationAdapter<String, Object>() {
			@Override
			public Object get(final String o) throws DeSerializationFailedException {
				return o;
			}
		};

		// Act
		clientStart.addFallBackDeSerialization(adapter);

		// Assert
		verify(clientStart.client).addFallBackDeSerialization(eq(adapter));
	}

	@Test
	public void setMainSerializationAdapter() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		clientStart.client = mock(Client.class);
		SerializationAdapter<Object, String> adapter = new SerializationAdapter<Object, String>() {
			@Override
			public String get(final Object o) throws SerializationFailedException {
				return o.toString();
			}
		};

		// Act
		clientStart.setMainSerializationAdapter(adapter);

		// Assert
		verify(clientStart.client).setMainSerializationAdapter(eq(adapter));
	}

	@Test
	public void setMainDeSerializationAdapter() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		clientStart.client = mock(Client.class);
		DeSerializationAdapter<String, Object> adapter = new DeSerializationAdapter<String, Object>() {
			@Override
			public Object get(final String o) throws DeSerializationFailedException {
				return o;
			}
		};

		// Act
		clientStart.setMainDeSerializationAdapter(adapter);

		// Assert
		verify(clientStart.client).setMainDeSerializationAdapter(eq(adapter));
	}

	@Test
	public void addDisconnectedHandler() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		clientStart.client = mock(Client.class);
		DisconnectedHandler disconnectedHandler = new DisconnectedHandler() {
			@Override
			public void handle(final Client client) {
			}
		};

		// Act
		clientStart.addDisconnectedHandler(disconnectedHandler);

		// Assert
		verify(clientStart.client).addDisconnectedHandler(eq(disconnectedHandler));
	}

	@Test
	public void setDecryptionAdapter() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		clientStart.client = mock(Client.class);
		DecryptionAdapter decryptionAdapter = new DecryptionAdapter() {
			@Override
			public String get(final String s) {
				return s;
			}
		};

		// Act
		clientStart.setDecryptionAdapter(decryptionAdapter);

		// Assert
		verify(clientStart.client).setDecryptionAdapter(eq(decryptionAdapter));
	}

	@Test
	public void setEncryptionAdapter() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		clientStart.client = mock(Client.class);
		EncryptionAdapter encryptionAdapter = new EncryptionAdapter() {
			@Override
			public String get(final String s) {
				return s;
			}
		};

		// Act
		clientStart.setEncryptionAdapter(encryptionAdapter);

		// Assert
		verify(clientStart.client).setEncryptionAdapter(eq(encryptionAdapter));
	}

	@Test
	public void getCommunicationRegistration() throws Exception {
		// Arrange
		ClientStart clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);

		// Act
		CommunicationRegistration communicationRegistration = clientStart.getCommunicationRegistration();

		// Assert
		assertNotNull(communicationRegistration);
	}

	@Test
	public void getCommunicationRegistration1() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		clientStart.client = mock(Client.class);
		when(clientStart.client.primed()).thenReturn(Synchronize.empty());

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
		ClientStart clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		final AtomicBoolean success = new AtomicBoolean(false);
		CacheObserver<TestSendObject> observer = new AbstractCacheObserver<TestSendObject>(TestSendObject.class) {
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
		};

		// Act
		clientStart.cache().addCacheObserver(observer);
		Cache cache = clientStart.cache();
		clientStart.clearCache();

		// Assert
		assertEquals(cache, clientStart.cache());
		assertEquals(0, cache.countObservers());
	}

	@Test(expected = RemoteObjectNotRegisteredException.class)
	public void getRemoteObject() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		clientStart.client = mock(Client.class);
		when(clientStart.client.send(any())).thenThrow(new SendFailedException(""));

		// Act
		RemoteTest object = clientStart.getRemoteObject(RemoteTest.class);
		assertNotNull(object);
		object.doSomething();

		// Assert
		fail();
	}

	@Test
	public void getRemoteObjectFactory() throws Exception {
		// Arrange
		ClientStart clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);

		// Act
		RemoteObjectFactory factory = clientStart.getRemoteObjectFactory();

		// Assert
		assertNotNull(factory);
	}

	@Test(expected = IllegalStateException.class)
	public void updateRemoteInvocationProducer() throws Exception {
		// Arrange
		ClientStart clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);

		// Act
		clientStart.updateRemoteInvocationProducer(new InvocationHandlerProducer() {
			@Override
			public <T> JavaRemoteInformationInvocationHandler<T> produce(final UUID uuid, final Class<T> clazz) {
				return null;
			}
		});

		// Assert
		clientStart.getRemoteObject(RemoteTest.class);
		fail();
	}

	private interface RemoteTest {
		void doSomething();
	}

	private class TestSendObject {
	}

}