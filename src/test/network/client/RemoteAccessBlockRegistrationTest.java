package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationResponse;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertEquals;

@Testing(RemoteAccessBlockRegistration.class)
public class RemoteAccessBlockRegistrationTest {
	@Test
	public void clearSemaphore() throws Exception {
		// Arrange
		RemoteAccessBlockRegistration registration = new RemoteAccessBlockRegistration();
		UUID id = UUID.randomUUID();

		// Act
		Semaphore semaphore = registration.await(new RemoteAccessCommunicationRequest("", Example.class, id, new Object[0]));
		registration.release(new RemoteAccessCommunicationResponse(id, null, null));
		registration.clearSemaphore(id);

		// Assert
		assertEquals(1, semaphore.availablePermits());
		assertEquals(0, registration.countSemaphores());
		assertEquals(1, registration.countResponses());
	}

	@Test
	public void clearResult() throws Exception {
		// Arrange
		RemoteAccessBlockRegistration registration = new RemoteAccessBlockRegistration();
		UUID id = UUID.randomUUID();

		// Act
		Semaphore semaphore = registration.await(new RemoteAccessCommunicationRequest("", Example.class, id, new Object[0]));
		registration.release(new RemoteAccessCommunicationResponse(id, null, null));
		registration.clearResult(id);

		// Assert
		assertEquals(1, semaphore.availablePermits());
		assertEquals(1, registration.countSemaphores());
		assertEquals(0, registration.countResponses());
	}

	@Test
	public void await() throws Exception {
		// Arrange
		RemoteAccessBlockRegistration registration = new RemoteAccessBlockRegistration();
		UUID id = UUID.randomUUID();

		// Act
		Semaphore semaphore = registration.await(new RemoteAccessCommunicationRequest("", Example.class, id, new Object[0]));

		// Assert
		assertEquals(0, semaphore.availablePermits());
	}

	@Test
	public void release() throws Exception {
		// Arrange
		RemoteAccessBlockRegistration registration = new RemoteAccessBlockRegistration();
		UUID id = UUID.randomUUID();

		// Act
		Semaphore semaphore = registration.await(new RemoteAccessCommunicationRequest("", Example.class, id, new Object[0]));

		// Assert
		assertEquals(0, semaphore.availablePermits());
		registration.release(new RemoteAccessCommunicationResponse(id, null, null));
		assertEquals(1, semaphore.availablePermits());
	}

	@Test
	public void getResponse() throws Exception {
		// Arrange
		RemoteAccessBlockRegistration registration = new RemoteAccessBlockRegistration();
		UUID id = UUID.randomUUID();
		RemoteAccessCommunicationResponse response = new RemoteAccessCommunicationResponse(id, null, null);

		// Act
		Semaphore semaphore = registration.await(new RemoteAccessCommunicationRequest("", Example.class, id, new Object[0]));
		registration.release(response);
		RemoteAccessCommunicationResponse foundResponse = registration.getResponse(id);

		// Assert
		assertEquals(1, semaphore.availablePermits());
		assertEquals(1, registration.countSemaphores());
		assertEquals(1, registration.countResponses());
		assertEquals(response, foundResponse);
	}

	private class Example {
	}
}