package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultClientHandlerTest {

	@Test
	public void create() throws Exception {
		// Arrange
		ClientList list = mock(ClientList.class);
		Socket socket = mock(Socket.class);
		CommunicationRegistration communicationRegistration = mock(CommunicationRegistration.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		when(socket.getOutputStream()).thenReturn(mock(OutputStream.class));
		when(socket.getInputStream()).thenReturn(mock(InputStream.class));
		when(list.isOpen()).thenReturn(true);
		DefaultClientHandler handler = new DefaultClientHandler(list, communicationRegistration, distributorRegistration);

		// Act
		Client client = handler.create(socket);

		// Assert
		assertNotNull(client);
		verify(list).add(eq(client));
	}

	@Test(expected = NullPointerException.class)
	public void createNull() throws Exception {
		// Arrange
		ClientList list = mock(ClientList.class);
		CommunicationRegistration communicationRegistration = mock(CommunicationRegistration.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		when(list.isOpen()).thenReturn(true);
		DefaultClientHandler handler = new DefaultClientHandler(list, communicationRegistration, distributorRegistration);

		// Act
		handler.create(null);

		// Assert
		fail();
	}

	@Test
	public void handle() throws Exception {
		// Arrange
		ClientList list = mock(ClientList.class);
		CommunicationRegistration communicationRegistration = mock(CommunicationRegistration.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		Client client = mock(Client.class);
		when(list.isOpen()).thenReturn(true);
		when(client.primed()).thenReturn(Synchronize.empty());
		DefaultClientHandler handler = new DefaultClientHandler(list, communicationRegistration, distributorRegistration);
		handler.connection = mock(Connection.class);

		// Act
		handler.handle(client);

		// Assert
		verify(client).primed();
		verify(client).addDisconnectedHandler(any());
	}

	@Test(expected = NullPointerException.class)
	public void handleNull() throws Exception {
		// Arrange
		ClientList list = mock(ClientList.class);
		CommunicationRegistration communicationRegistration = mock(CommunicationRegistration.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		when(list.isOpen()).thenReturn(true);
		DefaultClientHandler handler = new DefaultClientHandler(list, communicationRegistration, distributorRegistration);

		// Act
		handler.handle(null);

		// Assert
		fail();
	}
}