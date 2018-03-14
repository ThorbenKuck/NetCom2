package com.github.thorbenkuck.netcom2.network.client;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class JavaInvocationHandlerProducerTest {
	@Test
	public void produce() throws Exception {
		// Arrange
		JavaInvocationHandlerProducer producer = new JavaInvocationHandlerProducer(mock(Sender.class), mock(RemoteAccessBlockRegistration.class));

		// Act
		JavaRemoteInformationInvocationHandler<TestInterface> test = producer.produce(UUID.randomUUID(), TestInterface.class);

		// Assert
		assertNotNull(test);
	}

	@Test
	public void produce1() throws Exception {
		// Arrange
		JavaInvocationHandlerProducer producer = new JavaInvocationHandlerProducer(mock(Sender.class), mock(RemoteAccessBlockRegistration.class));

		// Act
		JavaRemoteInformationInvocationHandler<TestInterface> test = producer.produce(null, TestInterface.class);

		// Assert
		assertNotNull(test);
	}

	private interface TestInterface {}
}