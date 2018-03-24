package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.TestUtils;
import com.github.thorbenkuck.netcom2.interfaces.RemoteObjectRegistration;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RemoteObjectRequestHandlerTest {
	@Test
	public void accept() throws Exception {
		// Arrange
		Connection connection = mock(Connection.class);
		Session session = mock(Session.class);
		RemoteAccessCommunicationRequest remoteAccessCommunicationRequest = new RemoteAccessCommunicationRequest("", RemoteInterface.class, UUID.fromString(TestUtils.UUID_SEED_1), new Object[1]);

		RemoteObjectRegistration remoteObjectRegistration = mock(RemoteObjectRegistration.class);
		RemoteObjectRequestHandler handler = new RemoteObjectRequestHandler(remoteObjectRegistration);

		// Act
		handler.accept(connection, session, remoteAccessCommunicationRequest);

		// Assert
		verify(connection).write(any());
		verify(remoteObjectRegistration).run(eq(remoteAccessCommunicationRequest));
	}

	@Test
	public void testSessionIrrelevant() throws Exception {
		// Arrange
		Connection connection = mock(Connection.class);
		RemoteAccessCommunicationRequest remoteAccessCommunicationRequest = new RemoteAccessCommunicationRequest("", RemoteInterface.class, UUID.fromString(TestUtils.UUID_SEED_1), new Object[1]);

		RemoteObjectRegistration remoteObjectRegistration = mock(RemoteObjectRegistration.class);
		RemoteObjectRequestHandler handler = new RemoteObjectRequestHandler(remoteObjectRegistration);

		// Act
		handler.accept(connection, null, remoteAccessCommunicationRequest);

		// Assert
		verify(connection).write(any());
		verify(remoteObjectRegistration).run(eq(remoteAccessCommunicationRequest));
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptConnectionNull() throws Exception {
		// Arrange
		RemoteAccessCommunicationRequest remoteAccessCommunicationRequest = new RemoteAccessCommunicationRequest("", RemoteInterface.class, UUID.fromString(TestUtils.UUID_SEED_1), new Object[1]);
		Session session = mock(Session.class);

		RemoteObjectRegistration remoteObjectRegistration = mock(RemoteObjectRegistration.class);
		RemoteObjectRequestHandler handler = new RemoteObjectRequestHandler(remoteObjectRegistration);

		// Act
		handler.accept(null, session, remoteAccessCommunicationRequest);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptRequestNull() throws Exception {
		// Arrange
		Session session = mock(Session.class);
		Connection connection = mock(Connection.class);

		RemoteObjectRegistration remoteObjectRegistration = mock(RemoteObjectRegistration.class);
		RemoteObjectRequestHandler handler = new RemoteObjectRequestHandler(remoteObjectRegistration);

		// Act
		handler.accept(connection, session, null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptConnectionAndRequestNull() throws Exception {
		// Arrange
		Session session = mock(Session.class);

		RemoteObjectRegistration remoteObjectRegistration = mock(RemoteObjectRegistration.class);
		RemoteObjectRequestHandler handler = new RemoteObjectRequestHandler(remoteObjectRegistration);

		// Act
		handler.accept(null, session, null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptSessionAndConnectionAndRequestNull() throws Exception {
		// Arrange
		RemoteObjectRegistration remoteObjectRegistration = mock(RemoteObjectRegistration.class);
		RemoteObjectRequestHandler handler = new RemoteObjectRequestHandler(remoteObjectRegistration);

		// Act
		handler.accept(null, null, null);

		// Assert
		fail();
	}

	private interface RemoteInterface {
	}
}