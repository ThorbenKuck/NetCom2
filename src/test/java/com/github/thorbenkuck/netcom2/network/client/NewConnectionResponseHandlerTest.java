package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NewConnectionResponseHandlerTest {
	@Test
	public void accept() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		ClientConnector connector = mock(ClientConnector.class);
		SocketFactory socketFactory = mock(SocketFactory.class);
		Sender sender = mock(Sender.class);
		when(client.primed()).thenReturn(Synchronize.empty());
		when(client.getFalseIDs()).thenReturn(Collections.singletonList(ClientID.create()));
		when(client.isConnectionPrepared(ConnectionKey.class)).thenReturn(true);
		NewConnectionResponseHandler handler = new NewConnectionResponseHandler(client, connector, socketFactory, sender);

		// Act
		handler.accept(new NewConnectionRequest(ConnectionKey.class));

		// Assert
		verify(client).newPrimation();
		verify(client).setConnection(eq(ConnectionKey.class), any());
		verify(client).removeFalseIDs(any());
		verify(client).notifyAboutPreparedConnection(eq(ConnectionKey.class));
		verify(connector).establishConnection(eq(ConnectionKey.class), eq(socketFactory));
		verify(sender, atLeastOnce()).objectToServer(any(NewConnectionInitializer.class), eq(ConnectionKey.class));
	}

	private class ConnectionKey {
	}
}