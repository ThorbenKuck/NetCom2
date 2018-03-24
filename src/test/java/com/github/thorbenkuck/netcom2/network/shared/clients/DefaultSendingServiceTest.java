package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.exceptions.SetupListenerException;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import org.junit.Test;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Some methods, like {@link com.github.thorbenkuck.netcom2.network.interfaces.SendingService#setup(OutputStream, BlockingQueue)}
 * cannot be tested successfully, because they are blocking and sure as hell indeterminate.
 * <p>
 * So, if any test-case is missing, think about, whether this test-case is testable or not.
 * <p>
 * If i missed any, you may submit it to github.
 */
public class DefaultSendingServiceTest {

	private DefaultSendingService createSendingService() {
		return new DefaultSendingService(new TestSerializationAdapter(), new HashSet<>(), new TestEncryptionAdapter());
	}

	@Test (expected = SetupListenerException.class)
	public void runNotSetUp() throws Exception {
		// Arrange
		DefaultSendingService sendingService = createSendingService();
		// Act
		sendingService.run();

		// Assert
		fail();
	}

	@Test
	public void addSendDoneCallback() throws Exception {
		// Arrange
		DefaultSendingService sendingService = createSendingService();

		// Act
		sendingService.addSendDoneCallback(object -> {
		});

		// Assert
		assertEquals(1, sendingService.callbacks.size());
	}

	@Test (expected = IllegalArgumentException.class)
	public void addSendDoneCallbackNull() throws Exception {
		// Arrange
		DefaultSendingService sendingService = createSendingService();

		// Act
		sendingService.addSendDoneCallback(null);

		// Assert
		fail();
	}

	@Test
	public void overrideSendingQueue() throws Exception {
		// Arrange
		DefaultSendingService sendingService = createSendingService();
		BlockingQueue queue = sendingService.toSend;

		// Act
		sendingService.overrideSendingQueue(new LinkedBlockingQueue<>());

		// Assert
		assertNull(queue);
		assertNotNull(sendingService.toSend);
	}

	@Test (expected = IllegalArgumentException.class)
	public void overrideSendingQueueWithNull() throws Exception {
		// Arrange
		DefaultSendingService sendingService = createSendingService();

		// Act
		sendingService.overrideSendingQueue(null);

		// Assert
		fail();
	}

	@Test (expected = IllegalArgumentException.class)
	public void setupMethodOutputStreamAndSendInterfaceNull() throws Exception {
		// Arrange
		DefaultSendingService sendingService = createSendingService();

		// Act
		sendingService.setup(null, null);

		// Assert
		fail();
	}

	@Test (expected = IllegalArgumentException.class)
	public void setupMethodOutputStreamNull() throws Exception {
		// Arrange
		DefaultSendingService sendingService = createSendingService();

		// Act
		sendingService.setup(null, new LinkedBlockingQueue<>());

		// Assert
		fail();
	}

	@Test (expected = IllegalArgumentException.class)
	public void setupMethodSendInterfaceNull() throws Exception {
		// Arrange
		DefaultSendingService sendingService = createSendingService();

		// Act
		sendingService.setup(mock(OutputStream.class), null);

		// Assert
		fail();
	}

	@Test
	public void setupMethod() throws Exception {
		// Arrange
		DefaultSendingService sendingService = createSendingService();
		OutputStream outputStream = mock(OutputStream.class);
		BlockingQueue<Object> sendInterface = new LinkedBlockingQueue<>();
		BlockingQueue<Object> prev = sendingService.toSend;

		// Act
		sendingService.setup(outputStream, sendInterface);

		// Assert
		assertNull(prev);
		assertNotNull(sendingService.toSend);
		assertEquals(sendInterface, sendingService.toSend);
		assertTrue(sendingService.isSetup());
	}

	@Test
	public void started() throws Exception {
		// Arrange
//		DefaultSendingService sendingService = createSendingService();

		// Act
		// No act, should be non-empty and blocking by default
//		Awaiting awaiting = sendingService.started();

		// Assert
		// TODO Test if the returned awaiting is empty. Can only be done, once this is merged
//		assertFalse(Synchronize.isEmpty();
	}

	@Test (expected = IllegalArgumentException.class)
	public void setConnectionIDSupplierNull() throws Exception {
		// Arrange
		DefaultSendingService sendingService = createSendingService();

		// Act
		sendingService.setConnectionIDSupplier(null);

		// Assert
		fail();
	}

	@Test (expected = IllegalArgumentException.class)
	public void setConnectionIDSupplierNullGet() throws Exception {
		// Arrange
		DefaultSendingService sendingService = createSendingService();

		// Act
		sendingService.setConnectionIDSupplier(() -> null);

		// Assert
		fail();
	}

	@Test
	public void setConnectionIDSupplier() throws Exception {
		// Arrange
		Logging logging = mock(Logging.class);
		NetComLogging.setLogging(logging);
		DefaultSendingService sendingService = createSendingService();

		// Act
		sendingService.setConnectionIDSupplier(() -> "LOCAL_TEST");
		sendingService.overrideSendingQueue(new LinkedBlockingQueue<>());

		// Assert
		verify(logging).warn(contains("LOCAL_TEST"));
	}

	@Test
	public void running() throws Exception {
		// Arrange
		DefaultSendingService sendingService = createSendingService();

		// Act
		boolean running = sendingService.running();

		// Assert
		assertFalse(running);
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