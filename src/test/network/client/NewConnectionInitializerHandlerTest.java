package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.TestUtils;
import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@Testing(NewConnectionInitializer.class)
public class NewConnectionInitializerHandlerTest {
	@Test
	public void accept() throws Exception {
		// Arrange
		ClientID clientID = ClientID.create();
		Client client = mock(Client.class);
		when(client.getID()).thenReturn(clientID);
		Connection connection = mock(Connection.class);
		NewConnectionInitializerHandler handler = new NewConnectionInitializerHandler(client);
		ClientID toDelete = ClientID.create();
		Session session = mock(Session.class);

		// Act
		handler.accept(connection, session, new NewConnectionInitializer(ConnectionKey.class, clientID, toDelete));

		// Assert
		verify(client).removeFalseID(toDelete);
		verify(client).setConnection(ConnectionKey.class, connection);
	}

	@Test
	public void acceptSessionNullButStillWorking() {
		// Arrange
		ClientID clientID = ClientID.fromString(TestUtils.UUID_SEED_1);
		ClientID toDelete = ClientID.fromString(TestUtils.UUID_SEED_2);
		Client client = mock(Client.class);
		when(client.getID()).thenReturn(clientID);
		Connection connection = mock(Connection.class);
		NewConnectionInitializerHandler handler = new NewConnectionInitializerHandler(client);

		// Act
		handler.accept(connection, null, new NewConnectionInitializer(ConnectionKey.class, clientID, toDelete));

		// Assert
		verify(client).removeFalseID(toDelete);
		verify(client).setConnection(ConnectionKey.class, connection);
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptConnectionNull() throws Exception {
		// Arrange
		NewConnectionInitializerHandler handler = new NewConnectionInitializerHandler(mock(Client.class));
		ClientID clientID = ClientID.fromString(TestUtils.UUID_SEED_1);
		ClientID toDelete = ClientID.fromString(TestUtils.UUID_SEED_2);

		// Act
		handler.accept(null, null, new NewConnectionInitializer(ConnectionKey.class, clientID, toDelete));

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptInitializerNull() throws Exception {
		// Arrange
		NewConnectionInitializerHandler handler = new NewConnectionInitializerHandler(mock(Client.class));

		// Act
		handler.accept(mock(Connection.class), null, null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptAllNull() throws Exception {
		// Arrange
		NewConnectionInitializerHandler handler = new NewConnectionInitializerHandler(mock(Client.class));

		// Act
		handler.accept(null, null, null);

		// Assert
		fail();
	}

	private class ConnectionKey {
	}

}