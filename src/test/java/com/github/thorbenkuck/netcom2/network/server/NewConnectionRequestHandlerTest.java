package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class NewConnectionRequestHandlerTest {
	@Test
	public void accept() throws Exception {
		// Arrange
		NewConnectionRequestHandler handler = new NewConnectionRequestHandler();
		Connection connection = mock(Connection.class);
		Session session = mock(Session.class);
		NewConnectionRequest request = new NewConnectionRequest(ConnectionKey.class);

		// Act
		handler.accept(connection, session, request);

		// Assert
		verify(connection).write(request);
	}

	@Test
	public void acceptSessionNullStillSuccessful() throws Exception {
		// Arrange
		NewConnectionRequestHandler handler = new NewConnectionRequestHandler();
		Connection connection = mock(Connection.class);
		NewConnectionRequest request = new NewConnectionRequest(ConnectionKey.class);

		// Act
		handler.accept(connection, null, request);

		// Assert
		verify(connection).write(request);
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptConnectionNull() throws Exception {
		// Arrange
		NewConnectionRequestHandler handler = new NewConnectionRequestHandler();
		Session session = mock(Session.class);
		NewConnectionRequest request = new NewConnectionRequest(ConnectionKey.class);

		// Act
		handler.accept(null, session, request);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptRequestNull() throws Exception {
		// Arrange
		NewConnectionRequestHandler handler = new NewConnectionRequestHandler();
		Connection connection = mock(Connection.class);
		Session session = mock(Session.class);

		// Act
		handler.accept(connection, session, null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptConnectionAndRequestNull() throws Exception {
		// Arrange
		NewConnectionRequestHandler handler = new NewConnectionRequestHandler();
		Session session = mock(Session.class);

		// Act
		handler.accept(null, session, null);

		// Assert
		fail();
	}

	private class ConnectionKey {
	}
}