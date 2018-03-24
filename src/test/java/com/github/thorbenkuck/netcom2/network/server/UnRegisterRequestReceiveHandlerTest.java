package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterResponse;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class UnRegisterRequestReceiveHandlerTest {

	@Test
	public void accept() throws Exception {
		// Arrange
		DistributorRegistration registration = mock(DistributorRegistration.class);
		Session session = mock(Session.class);
		UnRegisterRequest registerRequest = new UnRegisterRequest(TestKey.class);
		UnRegisterRequestReceiveHandler handler = new UnRegisterRequestReceiveHandler(registration);

		// Act
		handler.accept(session, registerRequest);

		// Assert
		verify(registration).removeRegistration(eq(TestKey.class), eq(session));
		verify(session).send(any(UnRegisterResponse.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptSessionNull() throws Exception {
		// Arrange
		DistributorRegistration registration = mock(DistributorRegistration.class);
		UnRegisterRequest registerRequest = new UnRegisterRequest(TestKey.class);
		UnRegisterRequestReceiveHandler handler = new UnRegisterRequestReceiveHandler(registration);

		// Act
		handler.accept(null, registerRequest);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptRequestNull() throws Exception {
		// Arrange
		DistributorRegistration registration = mock(DistributorRegistration.class);
		Session session = mock(Session.class);
		UnRegisterRequestReceiveHandler handler = new UnRegisterRequestReceiveHandler(registration);

		// Act
		handler.accept(session, null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptSessionAndRequestNull() throws Exception {
		// Arrange
		DistributorRegistration registration = mock(DistributorRegistration.class);
		UnRegisterRequestReceiveHandler handler = new UnRegisterRequestReceiveHandler(registration);

		// Act
		handler.accept(null, null);

		// Assert
		fail();
	}

	private class TestKey {
	}

}