package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationResponse;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RemoteAccessResponseHandlerTest {
	@Test
	public void accept() throws Exception {
		// Arrange
		RemoteAccessBlockRegistration registration = mock(RemoteAccessBlockRegistration.class);
		RemoteAccessResponseHandler handler = new RemoteAccessResponseHandler(registration);
		RemoteAccessCommunicationResponse response = new RemoteAccessCommunicationResponse(UUID.randomUUID(), null, null);

		// Act
		handler.accept(response);

		// Assert
		verify(registration).release(eq(response));
	}

}