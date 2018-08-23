package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@Testing(ClientConnectionEstablish.class)
public class ClientConnectionEstablishTest {
	@Test
	public void newFor() throws Exception {
		// Arrange
		Client client = mock(Client.class);
		ClientConnectionEstablish clientConnectionEstablish = new ClientConnectionEstablish();

		// Act
		clientConnectionEstablish.newFor(TestObject.class, client);

		// Assert
		verify(client, atLeastOnce()).send(argThat(newConnectionRequest -> newConnectionRequest.getClass().equals(NewConnectionRequest.class) && ((NewConnectionRequest) newConnectionRequest).getKey().equals(TestObject.class)));
	}

	private class TestObject {
	}
}