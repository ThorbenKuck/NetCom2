package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Testing;
import com.github.thorbenkuck.netcom2.exceptions.SetupListenerException;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@Testing(DefaultReceivingService.class)
public class DefaultReceivingServiceTest {

	private DefaultReceivingService createReceivingService() {
		return new DefaultReceivingService(mock(CommunicationRegistration.class), () -> string -> null, HashSet::new, () -> string -> string);
	}

	@Test(expected = SetupListenerException.class)
	public void runWithoutSetup() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();

		// Act
		receivingService.run();

		// Assert
		fail();
	}

	@Test
	public void running() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();

		// Act
		boolean running = receivingService.running();

		// Assert
		assertFalse(running);
	}

	@Test
	public void cleanUpCallBacks() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();
		List<Callback<Object>> prev = receivingService.callbacks;

		// Act
		receivingService.cleanUpCallBacks();

		// Assert
		assertNotNull(prev);
		assertNotNull(receivingService.callbacks);
		assertEquals(prev, receivingService.callbacks);
	}

	@Test
	public void cleanUpCallBacksNotRemovable() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();
		receivingService.callbacks.add(new Callback<Object>() {
			@Override
			public void accept(Object o) {
				fail();
			}

			@Override
			public boolean isRemovable() {
				return false;
			}
		});

		// Act
		receivingService.cleanUpCallBacks();

		// Assert
		assertEquals(1, receivingService.callbacks.size());
	}

	@Test
	public void cleanUpCallBacksRemovable() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();
		receivingService.callbacks.add(new Callback<Object>() {
			@Override
			public void accept(Object o) {
				fail();
			}

			@Override
			public boolean isRemovable() {
				return true;
			}
		});

		// Act
		receivingService.cleanUpCallBacks();

		// Assert
		assertTrue(receivingService.callbacks.isEmpty());
	}

	@Test
	public void cleanUpCallBacksRemovableAndNonRemovable() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();
		receivingService.callbacks.add(new Callback<Object>() {
			@Override
			public void accept(Object o) {
				fail();
			}

			@Override
			public boolean isRemovable() {
				return true;
			}
		});
		receivingService.callbacks.add(new Callback<Object>() {
			@Override
			public void accept(Object o) {
				fail();
			}

			@Override
			public boolean isRemovable() {
				return false;
			}
		});

		// Act
		receivingService.cleanUpCallBacks();

		// Assert
		assertEquals(1, receivingService.callbacks.size());
	}

	@Test
	public void addReceivingCallback() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();

		// Act
		receivingService.addReceivingCallback(o -> {
		});

		// Assert
		assertEquals(1, receivingService.callbacks.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void addReceivingCallbackNull() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();

		// Act
		receivingService.addReceivingCallback(null);

		// Assert
		fail();
	}

	@Test
	public void setupMethod() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();
		Connection connection = mock(Connection.class);
		when(connection.getInputStream()).thenReturn(mock(InputStream.class));
		Session session = mock(Session.class);

		// Act
		receivingService.setup(connection, session);

		// Assert
		verify(connection).getInputStream();
		assertTrue(receivingService.isSetup());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setupConnectionNull() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();

		// Act
		receivingService.setup(null, mock(Session.class));

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setupSessionNull() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();

		// Act
		receivingService.setup(mock(Connection.class), null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setupSessionAndConnectionNull() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();

		// Act
		receivingService.setup(null, null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setSessionNull() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();

		// Act
		receivingService.setSession(null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void onDisconnectNull() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();

		// Act
		receivingService.onDisconnect(null);

		// Assert
		fail();
	}

	@Test
	public void started() throws Exception {
		// Arrange
		DefaultReceivingService receivingService = createReceivingService();

		// Act
		Awaiting started = receivingService.started();

		// Assert
		assertFalse(Synchronize.isEmpty(started));
	}

}