package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.cache.AbstractCacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.DefaultConnection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
	public void setup() throws Exception {
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

	@Test (expected = StartFailedException.class)
	public void launchNeg() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(((port, address) -> null));

		// Act
		clientStart.launch();

		// Assert
		// Assert via expected Exception
	}

	@Test
	public void cache() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		final AtomicBoolean success = new AtomicBoolean(false);
		CacheObserver<TestSendObject> observer = new AbstractCacheObserver<TestSendObject>(TestSendObject.class) {
			@Override
			public void newEntry(final TestSendObject testSendObject, final CacheObservable observable) {
				success.set(! success.get());
			}

			@Override
			public void updatedEntry(final TestSendObject testSendObject, final CacheObservable observable) {
				success.set(! success.get());
			}

			@Override
			public void deletedEntry(final TestSendObject testSendObject, final CacheObservable observable) {
				success.set(! success.get());
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

	@Test (expected = IllegalStateException.class)
	public void createNewConnectionNeg() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		clientStart.client = mock(Client.class);

		// Act
		clientStart.createNewConnection(TestSendObject.class);

		// Assert
		// No assert, assert through expected Exception
	}

	@Test (expected = IllegalStateException.class)
	public void setSocketFactory() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		SocketFactory socketFactory = mock(SocketFactory.class);
		when(socketFactory.create(anyInt(), anyString())).thenThrow(new IllegalStateException());
		clientStart.setSocketFactory(socketFactory);
		clientStart.client = mock(Client.class);

		// Act
		clientStart.launch();

		// Assert
		// No assert, assert through expected Exception
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

	@Test (expected = SendFailedException.class)
	public void sendNeg() throws Exception {
		// Arrange
		ClientStartImpl clientStart = new ClientStartImpl(ADDRESS, PORT);
		clientStart.setSocketFactory(mockedSocketFactory);
		TestSendObject testSendObject = new TestSendObject();

		// Act
		clientStart.send().objectToServer(testSendObject);

		// Assert
		verify(clientStart.client).send(testSendObject);
	}

	@Test
	public void addFallBackSerialization() throws Exception {
	}

	@Test
	public void addFallBackDeSerialization() throws Exception {
	}

	@Test
	public void setMainSerializationAdapter() throws Exception {
	}

	@Test
	public void setMainDeSerializationAdapter() throws Exception {
	}

	@Test
	public void addDisconnectedHandler() throws Exception {
	}

	@Test
	public void setDecryptionAdapter() throws Exception {
	}

	@Test
	public void setEncryptionAdapter() throws Exception {
	}

	@Test
	public void getCommunicationRegistration() throws Exception {
	}

	@Test
	public void clearCache() throws Exception {
	}

	@Test
	public void setLogging() throws Exception {
	}

	@Test
	public void getRemoteObject() throws Exception {
	}

	@Test
	public void getRemoteObjectFactory() throws Exception {
	}

	@Test
	public void updateRemoteInvocationProducer() throws Exception {
	}

	private class TestSendObject {
	}

}