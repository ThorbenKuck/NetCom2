package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Testing(PingHandler.class)
public class PingHandlerTest {

	private final ClientID clientID = ClientID.create();

	@Test
	public void accept() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		Connection connection = mock(Connection.class);
		when(client.getID()).thenReturn(ClientID.create());
		PingHandler handler = new PingHandler(client);

		// Act
		handler.accept(connection, mock(Session.class), new Ping(clientID));

		// Assert
		verify(client).addFalseID(eq(clientID));
		verify(client).triggerPrimation();
		verify(connection).write(any(Ping.class));
	}

	@Test
	public void accept1() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		Connection connection = mock(Connection.class);
		when(client.getID()).thenReturn(ClientID.empty());
		PingHandler handler = new PingHandler(client);

		// Act
		handler.accept(connection, mock(Session.class), new Ping(clientID));

		// Assert
		verify(client).setID(eq(clientID));
		verify(client).triggerPrimation();
		verify(connection).write(any(Ping.class));
	}

}