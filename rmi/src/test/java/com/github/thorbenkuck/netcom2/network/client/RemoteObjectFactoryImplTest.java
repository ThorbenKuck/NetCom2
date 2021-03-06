package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.exceptions.RemoteObjectNotRegisteredException;
import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@Testing(NativeRemoteObjectFactory.class)
public class RemoteObjectFactoryImplTest {

	private static final String RETURN_VALUE = "!TEST_RETURN_VALUE!";

	@Test
	public void createRemoteObject() throws Exception {
		// Arrange
		Sender sender = mock(Sender.class);
		NativeRemoteObjectFactory remoteObjectFactory = new NativeRemoteObjectFactory(sender);

		// Act
		remoteObjectFactory.createRemoteObject(TestInterface.class);

		// Assert
		// Assert is in act, no Exception should be thrown
	}

	@Test
	public void createRemoteObject1() throws Exception {
		// Arrange
		Sender sender = mock(Sender.class);
		NativeRemoteObjectFactory remoteObjectFactory = new NativeRemoteObjectFactory(sender);

		// Act
		remoteObjectFactory.createRemoteObject(TestInterface.class, (Runnable) () -> {
		});

		// Assert
		// Assert is in act, no Exception should be thrown
	}

	@Test
	public void createRemoteObject2() throws Exception {
		// Arrange
		Sender sender = mock(Sender.class);
		NativeRemoteObjectFactory remoteObjectFactory = new NativeRemoteObjectFactory(sender);

		// Act
		remoteObjectFactory.createRemoteObject(TestInterface.class, new TestImpl());

		// Assert
		// Assert is in act, no Exception should be thrown
	}

	@Test
	public void getRemoteAccessBlockRegistration() throws Exception {
		// Arrange
		Sender sender = mock(Sender.class);
		NativeRemoteObjectFactory remoteObjectFactory = new NativeRemoteObjectFactory(sender);

		// Act
		RemoteAccessBlockRegistration registration = remoteObjectFactory.getRemoteAccessBlockRegistration();

		// Assert
		assertNotNull(registration);
	}

	@Test(expected = IllegalStateException.class)
	public void setInvocationHandlerProducer() throws Exception {
		// Arrange
		Sender sender = mock(Sender.class);
		NativeRemoteObjectFactory remoteObjectFactory = new NativeRemoteObjectFactory(sender);

		// Act
		remoteObjectFactory.setInvocationHandlerProducer(new InvocationHandlerProducer() {
			@Override
			public <T> JavaRemoteInformationInvocationHandler<T> produce(final UUID uuid, final Class<T> clazz) {
				return null;
			}
		});

		// Assert
		// IllegalStateException will be thrown
		remoteObjectFactory.create(TestInterface.class);
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setFallback() throws Exception {
		// Arrange
		Sender sender = mock(Sender.class);
		doThrow(new SendFailedException("MockedSendFailed")).when(sender).objectToServer(any());
		NativeRemoteObjectFactory remoteObjectFactory = new NativeRemoteObjectFactory(sender);

		// Act
		remoteObjectFactory.setFallback(TestInterface.class, () -> {
			throw new IllegalArgumentException("Should be thrown");
		});
		TestInterface testInterface = remoteObjectFactory.create(TestInterface.class);

		// Assert
		testInterface.test();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setDefaultFallback() throws Exception {
		// Arrange
		Sender sender = mock(Sender.class);
		doThrow(new SendFailedException("MockedSendFailed")).when(sender).objectToServer(any());
		NativeRemoteObjectFactory remoteObjectFactory = new NativeRemoteObjectFactory(sender);

		// Act
		remoteObjectFactory.setDefaultFallback(() -> {
			throw new IllegalArgumentException("Should be thrown");
		});
		TestInterface testInterface = remoteObjectFactory.create(TestInterface.class);

		// Assert
		testInterface.test();
	}

	@Test
	public void setFallbackInstance() throws Exception {
		// Arrange
		Sender sender = mock(Sender.class);
		doThrow(new SendFailedException("MockedSendFailed")).when(sender).objectToServer(any());
		NativeRemoteObjectFactory remoteObjectFactory = new NativeRemoteObjectFactory(sender);

		// Act
		remoteObjectFactory.setFallbackInstance(TestInterface.class, new TestImpl());
		TestInterface testInterface = remoteObjectFactory.create(TestInterface.class);

		// Assert
		assertEquals(RETURN_VALUE, testInterface.test());
	}

	@Test
	public void create() throws Exception {
		// Arrange
		Sender sender = mock(Sender.class);
		NativeRemoteObjectFactory remoteObjectFactory = new NativeRemoteObjectFactory(sender);

		// Act
		remoteObjectFactory.create(TestInterface.class);

		// Assert
		// Assert is in act, no Exception should be thrown
	}

	@Test
	public void create1() throws Exception {
		// Arrange
		Sender sender = mock(Sender.class);
		NativeRemoteObjectFactory remoteObjectFactory = new NativeRemoteObjectFactory(sender);

		// Act
		remoteObjectFactory.createRemoteObject(TestInterface.class, (Runnable) () -> {
		});

		// Assert
		// Assert is in act, no Exception should be thrown
	}

	@Test
	public void create2() throws Exception {
		// Arrange
		Sender sender = mock(Sender.class);
		NativeRemoteObjectFactory remoteObjectFactory = new NativeRemoteObjectFactory(sender);

		// Act
		remoteObjectFactory.createRemoteObject(TestInterface.class, new TestImpl());

		// Assert
		// Assert is in act, no Exception should be thrown
	}

	@Test(expected = RemoteObjectNotRegisteredException.class)
	public void createWithoutFallback() throws Exception {
		// Arrange
		Sender sender = mock(Sender.class);
		doThrow(new SendFailedException("MockedSendFailed")).when(sender).objectToServer(any());
		NativeRemoteObjectFactory remoteObjectFactory = new NativeRemoteObjectFactory(sender);

		// Act
		remoteObjectFactory.setDefaultFallback(() -> {
			throw new IllegalArgumentException("ShouldNotAppear");
		});
		TestInterface object = remoteObjectFactory.createWithoutFallback(TestInterface.class);

		// Assert
		object.test();
	}

	private interface TestInterface {
		String test();
	}

	private static class TestImpl implements TestInterface {
		@Override
		public String test() {
			return RETURN_VALUE;
		}
	}
}