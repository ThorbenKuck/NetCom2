package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Testing;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import com.github.thorbenkuck.netcom2.network.synchronization.DefaultSynchronize;
import org.junit.Before;
import org.junit.Test;

import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Testing(AbstractConnection.class)
public class AbstractConnectionTest {

	private Socket mockedSocket;
	private SendingService mockedSendingService;
	private ReceivingService mockedReceivingService;
	private Session mockedSession;
	private Synchronize sendWaiting;

	@Before
	public void setUp() {
		mockedSocket = mock(Socket.class);
		mockedSendingService = mock(SendingService.class);
		mockedReceivingService = mock(ReceivingService.class);
		mockedSession = mock(Session.class);
		sendWaiting = new DefaultSynchronize();
	}

	@Test
	public void close() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		connection.close();

		// Assert
		verify(mockedReceivingService).softStop();
		verify(mockedSendingService).softStop();
		verify(mockedSocket).close();
	}

	@Test
	public void setupMethod() throws Exception {
		// Arrange
		OutputStream outputStream = mock(OutputStream.class);
		when(mockedSocket.getOutputStream()).thenReturn(outputStream);
		AbstractConnection connection = new TestConnection();

		// Act
		connection.setup();

		// Assert
		verify(mockedReceivingService).setup(eq(connection), eq(mockedSession));
		verify(mockedReceivingService).addReceivingCallback(any());
		verify(mockedSendingService).setup(eq(outputStream), any());

		verify(mockedReceivingService, never()).softStop();
		verify(mockedSendingService, never()).softStop();
		verify(mockedSocket, never()).close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void removeOnDisconnectedConsumerNull() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		connection.removeOnDisconnectedConsumer(null);

		// Assert
		fail();
	}

	@Test
	public void removeOnDisconnectedConsumerNonExisting() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();
		Consumer<Connection> connectionConsumer = new Consumer<Connection>() {
			@Override
			public void accept(final Connection connection) {
				fail();
			}
		};

		// Act
		connection.removeOnDisconnectedConsumer(connectionConsumer);

		// Assert
		connection.close();
	}

	@Test
	public void removeOnDisconnectedConsumer() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();
		Consumer<Connection> connectionConsumer = new Consumer<Connection>() {
			@Override
			public void accept(final Connection connection) {
				fail();
			}
		};
		connection.addOnDisconnectedConsumer(connectionConsumer);

		// Act
		connection.removeOnDisconnectedConsumer(connectionConsumer);

		// Assert
		connection.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void writeNull() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		connection.write(null);

		// Assert
		fail();
	}

	@Test(expected = IllegalStateException.class)
	public void writeNotSetUp() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		connection.write(new TestSendObject());

		// Assert
		fail();
	}

	@Test(expected = IllegalStateException.class)
	public void writeSetUpButNotActive() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();
		connection.setup();

		// Act
		connection.write(new TestSendObject());

		// Assert
		fail();
	}

	@Test
	public void write() throws Exception {
		// Arrange
		when(mockedSocket.isConnected()).thenReturn(true);
		when(mockedSendingService.running()).thenReturn(true);
		when(mockedReceivingService.running()).thenReturn(true);
		AbstractConnection connection = new TestConnection();
		connection.setup();

		// Act
		connection.write(new TestSendObject());
		sendWaiting.synchronize();

		// Assert
		assertNotNull(connection.getSendInterface().remove());
	}

	@Test(expected = IllegalArgumentException.class)
	public void addObjectSendListenerNull() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		connection.addObjectSendListener(null);

		// Assert
		fail();
	}

	@Test
	public void addObjectSendListener() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();
		Callback<Object> callback = new Callback<Object>() {
			@Override
			public void accept(final Object o) {
				fail();
			}
		};

		// Act
		connection.addObjectSendListener(callback);

		// Assert
		verify(mockedSendingService).addSendDoneCallback(eq(callback));
	}

	@Test(expected = IllegalArgumentException.class)
	public void addObjectReceivedListenerNull() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		connection.addObjectReceivedListener(null);

		// Assert
		fail();
	}

	@Test
	public void addObjectReceivedListener() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();
		Callback<Object> callback = new Callback<Object>() {
			@Override
			public void accept(final Object o) {
				fail();
			}
		};

		// Act
		connection.addObjectReceivedListener(callback);

		// Assert
		verify(mockedReceivingService).addReceivingCallback(eq(callback));
	}

	@Test
	public void setThreadPool() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();
		Logging mockedLogging = mock(Logging.class);
		NetComLogging.setLogging(mockedLogging);

		// Act
		connection.setThreadPool(mock(ExecutorService.class));

		// Assert
		verify(mockedLogging).error(eq("This operation is not yet supported!"));
	}

	@Test
	public void startListening() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();
		connection.setup();
		when(mockedReceivingService.started()).thenReturn(Synchronize.empty());
		when(mockedSendingService.started()).thenReturn(Synchronize.empty());

		// Act
		connection.startListening().synchronize();

		// Assert
//		verify(mockedSendingService).run();
		verify(mockedSendingService).setConnectionIDSupplier(any());
		verify(mockedSendingService).started();
//		verify(mockedReceivingService).run();
		verify(mockedReceivingService).onDisconnect(any(Runnable.class));
		verify(mockedReceivingService).started();
	}

	@Test(expected = IllegalArgumentException.class)
	public void addOnDisconnectedConsumerNull() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		connection.addOnDisconnectedConsumer(null);

		// Assert
		fail();
	}

	@Test
	public void addOnDisconnectedConsumer() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();
		connection.setup();
		when(mockedReceivingService.started()).thenReturn(Synchronize.empty());
		when(mockedSendingService.started()).thenReturn(Synchronize.empty());
		AtomicReference<Runnable> reference = new AtomicReference<>();
		doAnswer(invocation -> {
			reference.set(invocation.getArgument(0));
			return null;
		}).when(mockedReceivingService).onDisconnect(any());
		AtomicBoolean finished = new AtomicBoolean(false);
		Consumer<Connection> connectionConsumer = new Consumer<Connection>() {
			@Override
			public void accept(final Connection disconnected) {
				assertSame(connection, disconnected);
				finished.set(true);
			}
		};

		// Act
		connection.addOnDisconnectedConsumer(connectionConsumer);
		connection.startListening().synchronize();

		// Assert
		assertNotNull(reference.get());
		reference.get().run();
		assertTrue(finished.get());
	}

	@Test
	public void getInputStream() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		connection.getInputStream();

		// Assert
		verify(mockedSocket).getInputStream();
	}

	@Test
	public void getOutputStream() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		connection.getOutputStream();

		// Assert
		verify(mockedSocket).getOutputStream();
	}

	@Test
	public void getSendInterfaceEmpty() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		BlockingQueue<Object> sendInterface = connection.getSendInterface();

		// Assert
		assertNotNull(sendInterface);
		assertTrue(sendInterface.isEmpty());
	}

	@Test
	public void getSendInterfaceWithOneElement() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();
		connection.setup();
		when(mockedSocket.isConnected()).thenReturn(true);
		when(mockedSendingService.running()).thenReturn(true);
		when(mockedReceivingService.running()).thenReturn(true);
		connection.write(new TestSendObject());
		sendWaiting.synchronize();

		// Act
		BlockingQueue<Object> sendInterface = connection.getSendInterface();

		// Assert
		assertNotNull(sendInterface);
		assertFalse(sendInterface.isEmpty());
	}

	@Test
	public void getSession() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		Session session = connection.getSession();

		// Assert
		assertNotNull(session);
	}

	@Test
	public void setSession() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();
		Session session = Session.createNew(mock(Client.class));

		// Act
		connection.setSession(session);

		// Assert
		assertNotNull(connection.getSession());
		assertEquals(session, connection.getSession());
	}

	@Test
	public void getFormattedAddress() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		connection.getFormattedAddress();

		// Assert
		verify(mockedSocket).getPort();
		verify(mockedSocket).getInetAddress();
	}

	@Test
	public void getPort() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		connection.getFormattedAddress();

		// Assert
		verify(mockedSocket).getPort();
	}

	@Test
	public void getInetAddress() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		connection.getInetAddress();

		// Assert
		verify(mockedSocket).getInetAddress();
	}

	@Test
	public void isActiveDefault() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		// No act by default should be false

		// Assert
		assertFalse(connection.isActive());
	}

	@Test
	public void isActiveTrue() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();
		when(mockedSocket.isConnected()).thenReturn(true);
		when(mockedSendingService.running()).thenReturn(true);
		when(mockedReceivingService.running()).thenReturn(true);

		// Act
		// No act should be true now

		// Assert
		assertTrue(connection.isActive());
	}

	@Test
	public void getKey() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		// No act, is constructor parameter

		// Assert
		assertEquals(TestConnection.class, connection.getKey());
	}

	@Test
	public void setKey() throws Exception {
		// Arrange
		AbstractConnection connection = new TestConnection();

		// Act
		connection.setKey(TestSendObject.class);

		// Assert
		assertEquals(TestSendObject.class, connection.getKey());
	}

	private class TestConnection extends AbstractConnection {

		TestConnection() {
			super(mockedSocket, mockedSendingService, mockedReceivingService, mockedSession, TestConnection.class);
		}

		/**
		 * This method is called, before an Object is beforeSend to the client.
		 *
		 * @param o the Object
		 */
		@Override
		protected void beforeSend(final Object o) {

		}

		/**
		 * This method is called if an object is received and after its Communication is triggered
		 *
		 * @param o the Object
		 */
		@Override
		void receivedObject(final Object o) {

		}

		/**
		 * This method is only called, if the Connection is closed.
		 * <p>
		 * To be exact, it will be called AFTER the closing routing
		 */
		@Override
		protected void onClose() {

		}

		/**
		 * This method is called, after an Object has been send
		 *
		 * @param o the Object that just was send.
		 */
		@Override
		protected void afterSend(final Object o) {
			sendWaiting.goOn();
		}
	}

	private class TestSendObject {
	}

}