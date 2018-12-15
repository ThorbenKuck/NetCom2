package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.TestUtils;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

@Testing(JavaInvocationHandlerProducer.class)
public class JavaInvocationHandlerProducerTest {
	@Test
	public void produce() throws Exception {
		// Arrange
		JavaInvocationHandlerProducer producer = new JavaInvocationHandlerProducer(mock(Sender.class), mock(RemoteAccessBlockRegistration.class));

		// Act
		JavaRemoteInformationInvocationHandler<TestInterface> test = producer.produce(UUID.fromString(TestUtils.UUID_SEED_1), TestInterface.class);

		// Assert
		assertNotNull(test);
	}

	@Test(expected = IllegalArgumentException.class)
	public void produceUUIDNull() throws Exception {
		// Arrange
		JavaInvocationHandlerProducer producer = new JavaInvocationHandlerProducer(mock(Sender.class), mock(RemoteAccessBlockRegistration.class));

		// Act
		producer.produce(null, TestInterface.class);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void produceClassNull() throws Exception {
		// Arrange
		JavaInvocationHandlerProducer producer = new JavaInvocationHandlerProducer(mock(Sender.class), mock(RemoteAccessBlockRegistration.class));

		// Act
		producer.produce(UUID.fromString(TestUtils.UUID_SEED_1), null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void produceUUIDAndClassNull() throws Exception {
		// Arrange
		JavaInvocationHandlerProducer producer = new JavaInvocationHandlerProducer(mock(Sender.class), mock(RemoteAccessBlockRegistration.class));

		// Act
		producer.produce(null, null);

		// Assert
		fail();
	}

	private interface TestInterface {
	}
}