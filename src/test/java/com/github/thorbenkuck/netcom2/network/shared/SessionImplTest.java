package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.netcom2.interfaces.SendBridge;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.heartbeat.HeartBeat;
import com.github.thorbenkuck.netcom2.network.shared.heartbeat.HeartBeatParallel;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SessionImplTest {

	private SendBridge bridge;

	@Before
	public void before() {
		bridge = mock(SendBridge.class);
	}

	@Test
	public void isIdentified() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		boolean isIdentified = session.isIdentified();

		// Assert
		assertFalse(isIdentified);
	}

	@Test
	public void setIdentified() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		session.setIdentified(true);

		// Assert
		assertTrue(session.isIdentified());
	}

	@Test
	public void getIdentifier() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		String identifier = session.getIdentifier();

		// Assert
		assertEquals("", identifier);
	}

	@Test
	public void setIdentifier() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		session.setIdentifier("TEST");

		// Assert
		assertEquals("TEST", session.getIdentifier());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setIdentifierNull() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		session.setIdentifier(null);

		// Assert
		fail();
	}

	@Test
	public void getProperties() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		Properties properties = session.getProperties();

		// Assert
		assertTrue(properties.isEmpty());
	}

	@Test
	public void setProperties() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);
		Properties newProperties = new Properties();

		// Act
		session.setProperties(newProperties);

		// Assert
		assertEquals(newProperties, session.getProperties());
		assertSame(newProperties, session.getProperties());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setPropertiesNull() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		session.setProperties(null);

		// Assert
		fail();
	}

	@Test
	public void send() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);
		TestObject testObject = new TestObject();

		// Act
		session.send(testObject);

		// Assert
		verify(bridge).send(eq(testObject));
	}

	@Test(expected = IllegalArgumentException.class)
	public void sendNull() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		session.send(null);

		// Assert
		fail();
	}

	@Test
	public void eventOf() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		Pipeline<TestObject> pipeline = session.eventOf(TestObject.class);
		pipeline.addFirst(testObject -> {
		});

		// Assert
		assertNotNull(pipeline);
		assertEquals(pipeline, session.eventOf(TestObject.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void eventOfNull() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		session.eventOf(null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void triggerEventNotRegistered() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		session.triggerEvent(TestObject.class, new TestObject());

		// Assert
		fail();
	}

	@Test
	public void triggerEvent() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);
		TestObject testObject = new TestObject();
		AtomicBoolean finished = new AtomicBoolean(false);
		session.eventOf(TestObject.class).addFirst(o -> {
			assertEquals(testObject, o);
			finished.set(true);
		});

		// Act
		session.triggerEvent(TestObject.class, testObject);

		// Assert
		assertTrue(finished.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void triggerEventObjectNull() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		session.triggerEvent(TestObject.class, null);

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void triggerEventClassNull() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		session.triggerEvent(null, new TestObject());

		// Assert
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void triggerEventClassAndObjectNull() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		session.triggerEvent(null, new TestObject());

		// Assert
		fail();
	}

	@Test
	public void addHeartBeat() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);
		HeartBeat<Session> heartBeat = mock(HeartBeat.class);
		HeartBeatParallel<Session> parallel = mock(HeartBeatParallel.class);
		when(heartBeat.parallel()).thenReturn(parallel);

		// Act
		session.addHeartBeat(heartBeat);

		// Assert
		verify(heartBeat).parallel();
		verify(parallel).run(eq(session));
	}

	@Test(expected = IllegalArgumentException.class)
	public void addHeartBeatNull() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		session.addHeartBeat(null);

		// Assert
		fail();
	}

	@Test
	public void removeHeartBeatNotRegistered() throws Exception {
		// Arrange
		Logging logging = mock(Logging.class);
		NetComLogging.setLogging(logging);
		SessionImpl session = new SessionImpl(bridge);
		HeartBeat<Session> heartBeat = mock(HeartBeat.class);

		// Act
		session.removeHeartBeat(heartBeat);

		// Assert
		verify(logging).warn(contains("was never set"));
	}

	@Test
	public void removeHeartBeat() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);
		HeartBeat<Session> heartBeat = mock(HeartBeat.class);
		when(heartBeat.parallel()).thenReturn(mock(HeartBeatParallel.class));
		session.addHeartBeat(heartBeat);

		// Act
		session.removeHeartBeat(heartBeat);

		// Assert
		verify(heartBeat).stop();
	}

	@Test(expected = IllegalArgumentException.class)
	public void removeHeartBeatNull() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		session.removeHeartBeat(null);

		// Assert
		fail();
	}

	@Test
	public void primed() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		Awaiting awaiting = session.primed();

		// Assert
		assertFalse(Synchronize.isEmpty(awaiting));
	}

	@Test
	public void newPrimation() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);
		Awaiting awaiting = session.primed();
		session.triggerPrimation();

		// Act
		session.newPrimation();

		// Assert
		assertSame(awaiting, session.primed());
	}

	@Test
	public void update() throws Exception {
		// Arrange
		SessionImpl session = new SessionImpl(bridge);

		// Act
		SessionUpdater updater = session.update();

		// Assert
		assertNotNull(updater);
	}

	private class TestObject {

	}
}