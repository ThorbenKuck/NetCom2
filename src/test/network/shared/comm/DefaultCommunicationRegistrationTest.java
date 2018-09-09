package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import com.github.thorbenkuck.netcom2.pipeline.Wrapper;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * trigger1 does not need to be checked against null. The method trigger will check those to.
 */
@Testing(DefaultCommunicationRegistration.class)
public class DefaultCommunicationRegistrationTest {
	@Test
	public void register() throws Exception {
		// Arrange
		DefaultCommunicationRegistration communicationRegistration = new DefaultCommunicationRegistration();

		// Act
		ReceivePipeline<TestObject> pipeline = communicationRegistration.register(TestObject.class);

		// Assert
		assertNotNull(pipeline);
		assertTrue(pipeline.isEmpty());
		assertNotNull(communicationRegistration.map().get(TestObject.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerNull() throws Exception {
		// Arrange
		DefaultCommunicationRegistration communicationRegistration = new DefaultCommunicationRegistration();

		// Act
		communicationRegistration.register(null);

		// Assert
		fail();
	}

	@Test
	public void unRegisterRegistered() throws Exception {
		// Arrange
		DefaultCommunicationRegistration communicationRegistration = new DefaultCommunicationRegistration();
		communicationRegistration.register(TestObject.class);

		// Act
		communicationRegistration.unRegister(TestObject.class);

		// Assert
		assertNull(communicationRegistration.map().get(TestObject.class));
	}

	@Test
	public void unRegisterNotRegistered() throws Exception {
		// Arrange
		Logging logging = mock(Logging.class);
		NetComLogging.setLogging(logging);
		DefaultCommunicationRegistration communicationRegistration = new DefaultCommunicationRegistration();

		// Act
		communicationRegistration.unRegister(TestObject.class);

		// Assert
		verify(logging).warn(any());
	}

	@Test(expected = IllegalArgumentException.class)
	public void unRegisterNull() throws Exception {
		// Arrange
		DefaultCommunicationRegistration communicationRegistration = new DefaultCommunicationRegistration();

		// Act
		communicationRegistration.unRegister(null);

		// Assert
		fail();
	}

	@Test
	public void isRegisteredNotRegistered() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();

		// Act
		boolean registered = registration.isRegistered(TestObject.class);

		// Assert
		assertFalse(registered);
	}

	@Test
	public void isRegistered() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		registration.register(TestObject.class);

		// Act
		boolean registered = registration.isRegistered(TestObject.class);

		// Assert
		assertTrue(registered);
	}

	@Test
	public void isRegisteredInjected() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		registration.mapping.put(TestObject.class, mock(ReceivePipeline.class));

		// Act
		boolean registered = registration.isRegistered(TestObject.class);

		// Assert
		assertTrue(registered);
	}

	/**
	 * This Test is not testable like it stands.
	 * <p>
	 * The reason is, that the DefaultCommunicationRegistration extracts the execution of the Pipeline into a new Thread.
	 * This means the registered Pipeline might or might not be executed before the Tests ends.
	 * <p>
	 * However, this Tests exists, because it should NEVER fail. Not if it is executed before the Test ends, nor if it
	 * is not.
	 *
	 * @throws Exception e
	 */
	@Test
	public void trigger() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		Connection connection = mock(Connection.class);
		Session session = mock(Session.class);
		ReceivePipeline<TestObject> pipeline = registration.register(TestObject.class);
		TestObject testObject = new TestObject();
		pipeline.addFirst(((connection1, session1, testObject1) -> {
			assertEquals(connection, connection1);
			assertEquals(session, session1);
			assertEquals(testObject, testObject1);
		}));

		// Act
		registration.trigger(connection, session, testObject);

		// Assert
		assertFalse(pipeline.isEmpty());
	}

	@Test(expected = CommunicationNotSpecifiedException.class)
	public void triggerNotRegistered() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		Connection connection = mock(Connection.class);
		Session session = mock(Session.class);
		TestObject testObject = new TestObject();

		// Act
		registration.trigger(connection, session, testObject);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void triggerObjectNull() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		Connection connection = mock(Connection.class);
		Session session = mock(Session.class);

		// Act
		registration.trigger(connection, session, null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void triggerConnectionNull() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		Session session = mock(Session.class);
		TestObject testObject = new TestObject();

		// Act
		registration.trigger(null, session, testObject);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void triggerSessionNull() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		Connection connection = mock(Connection.class);
		TestObject testObject = new TestObject();

		// Act
		registration.trigger(connection, null, testObject);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void triggerConnectionAndSessionNull() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		TestObject testObject = new TestObject();

		// Act
		registration.trigger(null, null, testObject);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void triggerConnectionAndSessionAndObjectNull() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();

		// Act
		registration.trigger(null, null, null);

		// Assert
		fail();
	}

	@Test(expected = CommunicationNotSpecifiedException.class)
	public void trigger1NotRegistered() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		Connection connection = mock(Connection.class);
		Session session = mock(Session.class);

		// Act
		registration.trigger(TestObject.class, connection, session, new TestObject());

		// Assert
		fail();
	}

	/**
	 * Since the class and the object are not equal, this will throw an IllegalArgumentException.
	 * <p>
	 * For that the inner class TestObject2 is required.
	 *
	 * @throws Exception e
	 */
	@Test(expected = IllegalArgumentException.class)
	public void trigger1NotMatchingTypeAndObject() throws Exception {
		// Arrange
		class TestObject2 {

		}
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		Connection connection = mock(Connection.class);
		Session session = mock(Session.class);

		// Act
		registration.trigger(TestObject2.class, connection, session, new TestObject());

		// Assert
		fail();
	}

	/**
	 * Since the class and the object are not equal, this will throw an IllegalArgumentException.
	 * <p>
	 * For that the inner class TestObject2 is required.
	 *
	 * @throws Exception e
	 */
	@Test(expected = IllegalArgumentException.class)
	public void trigger1NullClass() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		Connection connection = mock(Connection.class);
		Session session = mock(Session.class);

		// Act
		registration.trigger(null, connection, session, new TestObject());

		// Assert
		fail();
	}

	/*
	 * The following DefaultCommunicationHandler methods cannot be tested with assertThat.
	 * The reason is, that inside of the DefaultCommunicationRegistration everything is saved as an OnReceiveTriple.
	 *
	 * Every non-triple OnReceive will be encapsulated within an Wrapper. This wrapper delegates equals calls to
	 * the correct encapsulation.
	 */


	@Test
	public void addDefaultCommunicationHandler() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		OnReceiveSingle<Object> defaultComm = new OnReceiveSingle<Object>() {
			@Override
			public void accept(Object o) {
				assertNotNull(o);
			}

			@Override
			public void onAddFailed() {
				fail();
			}
		};

		// Act
		registration.addDefaultCommunicationHandler(defaultComm);

		// Assert
		assertTrue(registration.map().isEmpty());
		assertFalse(registration.listDefaultsCommunicationRegistration().isEmpty());
		assertEquals(1, registration.listDefaultsCommunicationRegistration().size());
		assertTrue(registration.listDefaultsCommunicationRegistration().contains(new Wrapper().wrap(defaultComm)));
	}

	@Test
	public void addDefaultCommunicationHandler1() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		OnReceive<Object> defaultComm = new OnReceive<Object>() {
			@Override
			public void accept(Session session, Object o) {
				assertNotNull(o);
				assertNotNull(session);
			}

			@Override
			public void onAddFailed() {
				fail();
			}
		};

		// Act
		registration.addDefaultCommunicationHandler(defaultComm);

		// Assert
		assertTrue(registration.map().isEmpty());
		assertFalse(registration.listDefaultsCommunicationRegistration().isEmpty());
		assertEquals(1, registration.listDefaultsCommunicationRegistration().size());
		assertTrue(registration.listDefaultsCommunicationRegistration().contains(new Wrapper().wrap(defaultComm)));
	}

	@Test
	public void addDefaultCommunicationHandler2() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		OnReceiveTriple<Object> defaultComm = new OnReceiveTriple<Object>() {
			@Override
			public void accept(Connection connection, Session session, Object o) {
				assertNotNull(o);
				assertNotNull(session);
				assertNotNull(connection);
			}

			@Override
			public void onAddFailed() {
				fail();
			}
		};

		// Act
		registration.addDefaultCommunicationHandler(defaultComm);

		// Assert
		assertTrue(registration.map().isEmpty());
		assertFalse(registration.listDefaultsCommunicationRegistration().isEmpty());
		assertEquals(1, registration.listDefaultsCommunicationRegistration().size());
		assertTrue(registration.listDefaultsCommunicationRegistration().contains(defaultComm));
	}

	@Test
	public void clearEmpty() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();

		// Act
		registration.clear();

		// Assert
		assertTrue(registration.map().isEmpty());
	}

	@Test
	public void clearRegistered() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		registration.register(TestObject.class);

		// Act
		registration.clear();

		// Assert
		assertTrue(registration.map().isEmpty());
	}

	@Test
	public void clearRegisteredAndDefaultCommunicationHandler() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		registration.register(TestObject.class);
		registration.addDefaultCommunicationHandler(o -> {
		});

		// Act
		registration.clear();

		// Assert
		assertTrue(registration.map().isEmpty());
		assertTrue(registration.listDefaultsCommunicationRegistration().isEmpty());
	}

	@Test
	public void clearAllEmptyPipelines() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		registration.register(TestObject.class);
		registration.addDefaultCommunicationHandler(o -> {
		});

		// Act
		registration.clearAllEmptyPipelines();

		// Assert
		assertEquals(0, registration.map().size());
	}

	@Test
	public void clearAllEmptyPipelinesOneNotEmpty() throws Exception {
		// Arrange
		class TestObject2 {
		}
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		registration.register(TestObject.class);
		registration.register(TestObject2.class).addFirst(o -> {
		});
		registration.addDefaultCommunicationHandler(o -> {
		});

		// Act
		registration.clearAllEmptyPipelines();

		// Assert
		assertEquals(1, registration.map().size());
		assertNotNull(registration.mapping.get(TestObject2.class));
	}

	@Test
	public void updateBy() throws Exception {
		// Arrange
		class TestObject2 {
		}
		DefaultCommunicationRegistration registrationOne = new DefaultCommunicationRegistration();
		DefaultCommunicationRegistration registrationBase = new DefaultCommunicationRegistration();
		registrationOne.register(TestObject.class);
		registrationOne.addDefaultCommunicationHandler(o -> {
		});
		registrationBase.register(TestObject2.class);

		// Act
		registrationBase.updateBy(registrationOne);

		// Assert
		assertNull(registrationBase.mapping.get(TestObject2.class));
		assertNotNull(registrationBase.mapping.get(TestObject.class));
		assertEquals(1, registrationBase.listDefaultsCommunicationRegistration().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void updateByNull() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registrationBase = new DefaultCommunicationRegistration();

		// Act
		registrationBase.updateBy(null);

		// Assert
		fail();
	}

	@Test
	public void map() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		registration.register(TestObject.class);

		// Act
		Map<Class, ReceivePipeline<?>> mapping = registration.map();

		// Assert
		assertNotSame(mapping, registration.mapping);
		assertEquals(mapping, registration.mapping);
	}

	@Test
	public void listDefaultsCommunicationRegistration() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();
		registration.addDefaultCommunicationHandler(o -> {
		});

		// Act
		List<OnReceiveTriple<Object>> handler = registration.listDefaultsCommunicationRegistration();

		// Assert
		assertFalse(handler.isEmpty());
		assertEquals(1, handler.size());
	}

	@Test
	public void listDefaultsCommunicationRegistrationDefaultEmpty() throws Exception {
		// Arrange
		DefaultCommunicationRegistration registration = new DefaultCommunicationRegistration();

		// Act
		List<OnReceiveTriple<Object>> handler = registration.listDefaultsCommunicationRegistration();

		// Assert
		assertTrue(handler.isEmpty());
	}

	private class TestObject {
	}

}