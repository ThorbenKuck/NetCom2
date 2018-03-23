package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.EncryptionAdapter;
import org.junit.Test;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class DefaultSendingServiceTest {

	@Test(expected = Error.class)
	public void runNotSetUp() throws Exception {
		// Arrange
		DefaultSendingService sendingService = new DefaultSendingService(new TestSerializationAdapter(), new HashSet<>(), new TestEncryptionAdapter());

		// Act
		sendingService.run();

		// Assert
		fail();
	}

	@Test
	public void addSendDoneCallback() throws Exception {
		// Arrange
		DefaultSendingService sendingService = new DefaultSendingService(new TestSerializationAdapter(), new HashSet<>(), new TestEncryptionAdapter());

		// Act
		sendingService.addSendDoneCallback(object -> {});

		// Assert
		fail();
	}

	@Test
	public void overrideSendingQueue() throws Exception {
		// Arrange

		// Act

		// Assert
		fail();
	}

	@Test
	public void setupMethod() throws Exception {
		// Arrange

		// Act

		// Assert
		fail();
	}

	@Test
	public void started() throws Exception {
		// Arrange

		// Act

		// Assert
		fail();
	}

	@Test
	public void setConnectionIDSupplier() throws Exception {
		// Arrange

		// Act

		// Assert
		fail();
	}

	@Test
	public void softStop() throws Exception {
		// Arrange

		// Act

		// Assert
		fail();
	}

	@Test
	public void running() throws Exception {
		// Arrange

		// Act

		// Assert
		fail();
	}

	private class TestEncryptionAdapter implements EncryptionAdapter {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String get(final String s) {
			return s;
		}
	}

	private class TestSerializationAdapter implements SerializationAdapter<Object, String> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String get(final Object o) throws SerializationFailedException {
			return o.toString();
		}
	}
}