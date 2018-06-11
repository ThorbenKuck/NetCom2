package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.Testing;
import com.github.thorbenkuck.netcom2.exceptions.PipelineAccessException;
import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationResponse;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Testing(JavaRemoteInformationInvocationHandler.class)
public class JavaRemoteInformationInvocationHandlerTest {

	final Semaphore semaphore = new Semaphore(1);

	private Semaphore getSemaphore() {
		return semaphore;
	}

	private Semaphore getAndAcquire() {
		Semaphore semaphore = getSemaphore();
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return semaphore;
	}

	private void continueSemaphore() {
		semaphore.release();
	}

	@Test
	public void invoke() throws Throwable {
		// Arrange
		UUID id = UUID.randomUUID();
		Sender sender = mock(Sender.class);
		when(sender.objectToServer(any(RemoteAccessCommunicationRequest.class))).thenAnswer(invocationOnMock -> {
			continueSemaphore();
			return null;
		});
		RemoteAccessBlockRegistration registration = mock(RemoteAccessBlockRegistration.class);
		when(registration.getResponse(any())).thenReturn(new RemoteAccessCommunicationResponse(UUID.randomUUID(), null, null));
		when(registration.await(any())).thenReturn(getAndAcquire());
		JavaRemoteInformationInvocationHandler<TestInterface> handler = new JavaRemoteInformationInvocationHandler<>(sender, registration, TestInterface.class, id);

		// Act
		Object result = handler.invoke(null, TestInterface.class.getMethod("testMethod"), new Object[0]);

		// Assert
		verify(sender, atLeastOnce()).objectToServer(any(RemoteAccessCommunicationRequest.class));
		assertNull(result);
	}

	@Test(expected = PipelineAccessException.class)
	public void setFallbackRunnable() throws Throwable {
		// Arrange
		UUID id = UUID.randomUUID();
		Sender sender = mock(Sender.class);
		when(sender.objectToServer(any())).thenThrow(new SendFailedException("MockedSendFailed"));
		RemoteAccessBlockRegistration registration = mock(RemoteAccessBlockRegistration.class);
		JavaRemoteInformationInvocationHandler<TestInterface> handler = new JavaRemoteInformationInvocationHandler<>(sender, registration, TestInterface.class, id);

		// Act
		handler.setFallbackRunnable(() -> {
			throw new PipelineAccessException("");
		});

		// Assert
		handler.invoke(null, TestInterface.class.getMethod("testMethod"), new Object[0]);
		fail();
	}

	@Test
	public void setFallbackInstance() throws Throwable {
		// Arrange
		UUID id = UUID.randomUUID();
		Sender sender = mock(Sender.class);
		AtomicBoolean done = new AtomicBoolean(false);
		when(sender.objectToServer(any())).thenThrow(new SendFailedException("MockedSendFailed"));
		RemoteAccessBlockRegistration registration = mock(RemoteAccessBlockRegistration.class);
		JavaRemoteInformationInvocationHandler<TestInterface> handler = new JavaRemoteInformationInvocationHandler<>(sender, registration, TestInterface.class, id);

		// Act
		handler.setFallbackInstance(() -> done.set(true));
		handler.invoke(null, TestInterface.class.getMethod("testMethod"), new Object[0]);

		// Assert
		verify(sender, atLeastOnce()).objectToServer(any(RemoteAccessCommunicationRequest.class));
		assertTrue(done.get());
	}

	private interface TestInterface {
		void testMethod();
	}

}