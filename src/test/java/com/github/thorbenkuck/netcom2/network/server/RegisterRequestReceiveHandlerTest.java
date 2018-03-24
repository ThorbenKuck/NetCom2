package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RegisterResponse;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RegisterRequestReceiveHandlerTest {
	@Test
	public void accept() throws Exception {
		// Arrange
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		Cache cache = mock(Cache.class);

		Session session = mock(Session.class);
		RegisterRequest registerRequest = new RegisterRequest(RegisterTest.class);

		RegisterRequestReceiveHandler registerRequestReceiveHandler = new RegisterRequestReceiveHandler(distributorRegistration, cache);

		// Act
		registerRequestReceiveHandler.accept(session, registerRequest);

		// Assert
		verify(distributorRegistration).addRegistration(eq(RegisterTest.class), eq(session));
		verify(session).send(ArgumentMatchers.any(RegisterResponse.class));
		verify(cache).get(eq(RegisterTest.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptSessionNull() throws Exception {
		// Arrange
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		Cache cache = mock(Cache.class);

		RegisterRequest registerRequest = new RegisterRequest(RegisterTest.class);

		RegisterRequestReceiveHandler registerRequestReceiveHandler = new RegisterRequestReceiveHandler(distributorRegistration, cache);

		// Act
		registerRequestReceiveHandler.accept(null, registerRequest);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptRequestNull() throws Exception {
		// Arrange
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		Cache cache = mock(Cache.class);

		Session session = mock(Session.class);
		RegisterRequestReceiveHandler registerRequestReceiveHandler = new RegisterRequestReceiveHandler(distributorRegistration, cache);

		// Act
		registerRequestReceiveHandler.accept(session, null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void acceptSessionAndRequestNull() throws Exception {
		// Arrange
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		Cache cache = mock(Cache.class);

		RegisterRequestReceiveHandler registerRequestReceiveHandler = new RegisterRequestReceiveHandler(distributorRegistration, cache);

		// Act
		registerRequestReceiveHandler.accept(null, null);

		// Assert
		fail();
	}

	private class RegisterTest {
	}

}