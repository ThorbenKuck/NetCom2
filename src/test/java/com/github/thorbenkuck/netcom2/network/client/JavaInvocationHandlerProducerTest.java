package com.github.thorbenkuck.netcom2.network.client;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;
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
		JavaRemoteInformationInvocationHandler<TestInterface> test = producer.produce(UUID.fromString("57f28dd6-29e8-11e8-b467-0ed5f89f718b"), TestInterface.class);

		// Assert
		assertNotNull(test);
	}

	private interface TestInterface {
	}
}