package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.ReceiveHandler;
import com.github.thorbenkuck.netcom2.exceptions.PipelineAccessException;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class QueuedReceivePipelineTest {

	private <T> QueuedReceivePipeline<T> create(Class<T> clazz) {
		return new QueuedReceivePipeline<>(clazz);
	}

	@Test
	public void addLast() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		pipeline.addLast(object -> object.value += 4);
		pipeline.addLast(object -> object.value *= 2);
		ReceivePipelineTestObject testObject = new ReceivePipelineTestObject();

		// Act
		pipeline.run(mock(Connection.class), mock(Session.class), testObject);

		// Assert
		assertEquals("(0 + 4) * 2 = 8", 8, testObject.value);
	}

	@Test
	public void addLast1() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		pipeline.addLast(object -> object.value *= 2);
		pipeline.addLast(object -> object.value += 4);
		ReceivePipelineTestObject testObject = new ReceivePipelineTestObject();

		// Act
		pipeline.run(mock(Connection.class), mock(Session.class), testObject);

		// Assert
		assertEquals("(0 * 2) + 4 = 4", 4, testObject.value);
	}

	@Test
	public void addLast2() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		pipeline.addFirst(object -> object.value += 4);
		pipeline.addLast(object -> object.value *= 2);
		ReceivePipelineTestObject testObject = new ReceivePipelineTestObject();

		// Act
		pipeline.run(mock(Connection.class), mock(Session.class), testObject);

		// Assert
		assertEquals("(0 + 4)[FIRST] * 2[LAST] = 8", 8, testObject.value);
	}

	@Test
	public void addFirst() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		pipeline.addFirst(object -> object.value += 4);
		pipeline.addFirst(object -> object.value *= 2);
		ReceivePipelineTestObject testObject = new ReceivePipelineTestObject();

		// Act
		pipeline.run(mock(Connection.class), mock(Session.class), testObject);

		// Assert
		assertEquals("(0 * 2) + 4 = 4", 4, testObject.value);
	}

	@Test
	public void addFirst1() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		pipeline.addFirst(object -> object.value *= 2);
		pipeline.addFirst(object -> object.value += 4);
		ReceivePipelineTestObject testObject = new ReceivePipelineTestObject();

		// Act
		pipeline.run(mock(Connection.class), mock(Session.class), testObject);

		// Assert
		assertEquals("(0 + 4) * 2 = 8", 8, testObject.value);
	}

	@Test
	public void addFirst2() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		pipeline.addFirst(object -> object.value += 2);
		ReceivePipelineTestObject testObject = new ReceivePipelineTestObject();

		// Act
		pipeline.run(mock(Connection.class), mock(Session.class), testObject);

		// Assert
		assertEquals("(0 + 2) = 2", 2, testObject.value);
	}

	@Test
	public void addFirstIfNotContained() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle = new OnReceiveSingleImpl<>(object -> object.value += 1);

		// Act
		pipeline.addFirstIfNotContained(onReceiveSingle);

		// Assert
		assertTrue(pipeline.contains(onReceiveSingle));
		assertEquals(1, pipeline.size());
	}

	@Test
	public void addFirstIfNotContained1() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle = new OnReceiveSingleImpl<>(object -> object.value += 1);

		// Act
		pipeline.addFirstIfNotContained(onReceiveSingle);
		pipeline.addFirstIfNotContained(onReceiveSingle);

		// Assert
		assertTrue(pipeline.contains(onReceiveSingle));
		assertEquals(1, pipeline.size());
	}

	@Test
	public void addFirstIfNotContained2() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle = new OnReceiveSingleImpl<>(object -> object.value += 1);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle2 = new OnReceiveSingleImpl<>(object -> object.value += 2);

		// Act
		pipeline.addFirstIfNotContained(onReceiveSingle);
		pipeline.addFirstIfNotContained(onReceiveSingle2);

		// Assert
		assertTrue(pipeline.contains(onReceiveSingle));
		assertTrue(pipeline.contains(onReceiveSingle2));
		assertEquals(2, pipeline.size());
	}

	@Test
	public void addLastIfNotContained() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle = new OnReceiveSingleImpl<>(object -> object.value += 1);

		// Act
		pipeline.addLastIfNotContained(onReceiveSingle);

		// Assert
		assertTrue(pipeline.contains(onReceiveSingle));
		assertEquals(1, pipeline.size());
	}

	@Test
	public void addLastIfNotContained1() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle = new OnReceiveSingleImpl<>(object -> object.value += 1);

		// Act
		pipeline.addLastIfNotContained(onReceiveSingle);
		pipeline.addLastIfNotContained(onReceiveSingle);

		// Assert
		assertTrue(pipeline.contains(onReceiveSingle));
		assertEquals(1, pipeline.size());
	}

	@Test
	public void addLastIfNotContained2() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle = new OnReceiveSingleImpl<>(object -> object.value += 1);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle2 = new OnReceiveSingleImpl<>(object -> object.value += 2);

		// Act
		pipeline.addLastIfNotContained(onReceiveSingle);
		pipeline.addLastIfNotContained(onReceiveSingle2);

		// Assert
		assertTrue(pipeline.contains(onReceiveSingle));
		assertTrue(pipeline.contains(onReceiveSingle2));
		assertEquals(2, pipeline.size());
	}

	@Test
	public void to() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		ReceivePipelineTestObject testObject = new ReceivePipelineTestObject();
		Handler handler = new Handler();
		handler.value = 4;

		// Act
		pipeline.to(handler);
		pipeline.run(mock(Connection.class), mock(Session.class), testObject);

		// Assert
		assertEquals(4, testObject.value);
	}

	@Test
	public void contains() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle = new OnReceiveSingleImpl<>(object -> object.value += 1);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle2 = new OnReceiveSingleImpl<>(object -> object.value += 1);

		// Act
		pipeline.addFirst(onReceiveSingle);

		// Assert
		assertTrue(pipeline.contains(onReceiveSingle));
		assertFalse(pipeline.contains(onReceiveSingle2));
	}

	@Test
	public void contains1() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle = new OnReceiveSingleImpl<>(object -> object.value += 1);

		// Act
		// No act, should ne be contained by default

		// Assert
		assertFalse(pipeline.contains(onReceiveSingle));
	}

	@Test
	public void contains2() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle = new OnReceiveSingleImpl<>(object -> object.value += 1);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle2 = new OnReceiveSingleImpl<>(object -> object.value += 1);

		// Act
		pipeline.addFirst(onReceiveSingle);
		pipeline.addFirst(onReceiveSingle2);

		// Assert
		assertTrue(pipeline.contains(onReceiveSingle));
		assertTrue(pipeline.contains(onReceiveSingle2));
	}

	@Test (expected = PipelineAccessException.class)
	public void isSealed() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);

		// Act
		pipeline.seal();

		// Assert
		assertTrue(pipeline.isSealed());
		pipeline.close();
		fail();
	}

	@Test (expected = PipelineAccessException.class)
	public void isClosed() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);

		// Act
		pipeline.close();

		// Assert
		assertTrue(pipeline.isClosed());
		pipeline.seal();
		pipeline.open();
		fail();
	}

	@Test
	public void isClosed1() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);

		// Act
		pipeline.close();

		// Assert
		assertTrue(pipeline.isClosed());
		pipeline.open();
		assertFalse(pipeline.isClosed());
	}

	@Test
	public void isEmpty() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);

		// Act
		// No act here, should be empty by default

		// Assert
		assertTrue(pipeline.isEmpty());
	}

	@Test
	public void isEmpty1() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle = new OnReceiveSingleImpl<>(object -> object.value += 1);

		// Act
		pipeline.addFirst(onReceiveSingle);

		// Assert
		assertFalse(pipeline.isEmpty());
	}

	@Test (expected = PipelineAccessException.class)
	public void ifClosed() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);

		// Act
		pipeline.close();

		// Assert
		pipeline.ifClosed(() -> {
			throw new PipelineAccessException("");
		});
		fail();
	}

	@Test
	public void ifClosed1() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);

		// Act
		// No act, should not be closed by default

		// Assert
		pipeline.ifClosed(() -> {
			throw new PipelineAccessException("");
		});
		assertFalse(pipeline.isClosed());
	}

	@Test (expected = PipelineAccessException.class)
	public void setReceivePipelineHandlerPolicy() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);

		// Act
		pipeline.setReceivePipelineHandlerPolicy(ReceivePipelineHandlerPolicy.NOT_ALLOWED);

		// Assert
		pipeline.to(new Handler());
		fail();
	}

	@Test (expected = PipelineAccessException.class)
	public void setReceivePipelineHandlerPolicy1() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);

		// Act
		pipeline.setReceivePipelineHandlerPolicy(ReceivePipelineHandlerPolicy.ALLOW_SINGLE);
		pipeline.to(new Handler());
		pipeline.to(new Handler());

		// Assert
		// Through Exception
		fail();
	}

	@Test
	public void setReceivePipelineHandlerPolicy2() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);

		// Act
		pipeline.setReceivePipelineHandlerPolicy(ReceivePipelineHandlerPolicy.ALLOW_MULTIPLE);
		pipeline.to(new Handler());
		pipeline.to(new Handler());

		// Assert
		assertEquals(2, pipeline.size());
	}

	@Test
	public void remove() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceive<ReceivePipelineTestObject> onReceive = new OnReceive<ReceivePipelineTestObject>() {
			@Override
			public void accept(final Session session, final ReceivePipelineTestObject receivePipelineTestObject) {
				// do nothing
			}
		};

		// Act
		pipeline.addFirst(onReceive);

		// Assert
		assertEquals(1, pipeline.size());
		pipeline.remove(onReceive);
		assertEquals(0, pipeline.size());

	}

	@Test
	public void clear() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle = new OnReceiveSingleImpl<>(object -> object.value += 1);

		// Act
		pipeline.addFirst(onReceiveSingle);
		pipeline.addFirst(onReceiveSingle);
		pipeline.addFirst(onReceiveSingle);

		// Assert
		assertEquals(3, pipeline.size());
		pipeline.clear();
		assertEquals(0, pipeline.size());
	}

	@Test
	public void run() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle = new OnReceiveSingleImpl<>(object -> object.value += 1);
		ReceivePipelineTestObject testObject = new ReceivePipelineTestObject();

		// Act
		pipeline.addFirst(onReceiveSingle);
		pipeline.run(mock(Connection.class), mock(Session.class), testObject);

		// Assert
		assertEquals(1, testObject.value);
	}

	@Test
	public void close() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);
		OnReceiveSingle<ReceivePipelineTestObject> onReceiveSingle = new OnReceiveSingleImpl<>(object -> object.value += 1);

		// Act
		pipeline.close();

		// Assert
		assertFalse(pipeline.isSealed());
		assertTrue(pipeline.isClosed());
		pipeline.addFirst(onReceiveSingle);
		assertTrue(pipeline.isEmpty());
	}

	@Test (expected = PipelineAccessException.class)
	public void seal() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);

		// Act
		pipeline.seal();

		// Assert
		assertTrue(pipeline.isSealed());
		assertFalse(pipeline.isClosed());
		pipeline.close();
		fail();
	}

	@Test
	public void open() throws Exception {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = create(ReceivePipelineTestObject.class);

		// Act
		pipeline.close();

		// Assert
		assertTrue(pipeline.isClosed());
	}

	private class OnReceiveSingleImpl<T> implements OnReceiveSingle<T> {

		private final Consumer<T> consumer;

		private OnReceiveSingleImpl(final Consumer<T> consumer) {
			this.consumer = consumer;
		}

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param t the input argument
		 */
		@Override
		public void accept(final T t) {
			consumer.accept(t);
		}
	}

	private class Handler {

		int value;

		@ReceiveHandler
		public void handle(ReceivePipelineTestObject object) {
			object.value = value;
		}
	}

}