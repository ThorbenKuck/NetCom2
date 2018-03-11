package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
		handler.accept(connection, null, new Ping(clientID));

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
		handler.accept(connection, null, new Ping(clientID));

		// Assert
		verify(client).setID(eq(clientID));
		verify(client).triggerPrimation();
		verify(connection).write(any(Ping.class));
	}

}