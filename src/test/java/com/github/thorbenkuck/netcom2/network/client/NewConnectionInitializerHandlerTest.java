package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import org.junit.Test;

import static org.mockito.Mockito.*;

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

		// Act
		handler.accept(connection, null, new NewConnectionInitializer(ConnectionKey.class, clientID, toDelete));

		// Assert
		verify(client).removeFalseID(toDelete);
		verify(client).setConnection(ConnectionKey.class, connection);
	}

	private class ConnectionKey {
	}

}