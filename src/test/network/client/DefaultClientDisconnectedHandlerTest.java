package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@Testing(DefaultClientDisconnectedHandler.class)
public class DefaultClientDisconnectedHandlerTest {
	@Test
	public void handle() throws Exception {
		// Arrange
		AtomicBoolean launched = new AtomicBoolean(false);
		ClientStartImpl clientStart = mock(ClientStartImpl.class);
		when(clientStart.cache()).thenReturn(mock(Cache.class));
		when(clientStart.send()).thenReturn(mock(Sender.class));
		when(clientStart.launched()).thenReturn(launched);
		Client client = mock(Client.class);
		DefaultClientDisconnectedHandler handler = new DefaultClientDisconnectedHandler(clientStart);

		// Act
		handler.handle(client);

		// Assert
		verify(clientStart, atLeastOnce()).cache();
		verify(clientStart, atLeastOnce()).send();
		verify(client, atLeastOnce()).clearSession();
		verify(client, atLeastOnce()).setup();

		assertFalse(launched.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void handleNull() throws Exception {

		// Arrange
		ClientStartImpl clientStart = mock(ClientStartImpl.class);
		DefaultClientDisconnectedHandler handler = new DefaultClientDisconnectedHandler(clientStart);

		// Act
		handler.handle(null);

		// Assert
		fail();
	}

	@Test
	public void active() throws Exception {
		// Arrange
		ClientStartImpl clientStart = mock(ClientStartImpl.class);
		DefaultClientDisconnectedHandler handler = new DefaultClientDisconnectedHandler(clientStart);

		// Act
		// no act, should always be true

		// Assert
		assertTrue(handler.active());
	}

}