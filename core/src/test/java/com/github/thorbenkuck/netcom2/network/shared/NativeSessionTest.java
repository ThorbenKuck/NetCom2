package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Testing(NativeSession.class)
public class NativeSessionTest {

	private SendBridge bridge;

	@Before
	public void before() {
		bridge = mock(SendBridge.class);
	}

	@Test
	public void isIdentified() throws Exception {
		// Arrange
		NativeSession session = new NativeSession(bridge);

		// Act
		boolean isIdentified = session.isIdentified();

		// Assert
		assertFalse(isIdentified);
	}

	@Test
	public void setIdentified() throws Exception {
		// Arrange
		NativeSession session = new NativeSession(bridge);

		// Act
		session.setIdentified(true);

		// Assert
		assertTrue(session.isIdentified());
	}

	@Test
	public void getIdentifier() throws Exception {
		// Arrange
		NativeSession session = new NativeSession(bridge);

		// Act
		String identifier = session.getIdentifier();

		// Assert
		assertEquals("", identifier);
	}

	@Test
	public void setIdentifier() throws Exception {
		// Arrange
		NativeSession session = new NativeSession(bridge);

		// Act
		session.setIdentifier("TEST");

		// Assert
		assertEquals("TEST", session.getIdentifier());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setIdentifierNull() throws Exception {
		// Arrange
		NativeSession session = new NativeSession(bridge);

		// Act
		session.setIdentifier(null);

		// Assert
		fail();
	}

	@Test
	public void getProperties() throws Exception {
		// Arrange
		NativeSession session = new NativeSession(bridge);

		// Act
		Properties properties = session.getProperties();

		// Assert
		assertTrue(properties.isEmpty());
	}

	@Test
	public void setProperties() throws Exception {
		// Arrange
		NativeSession session = new NativeSession(bridge);
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
		NativeSession session = new NativeSession(bridge);

		// Act
		session.setProperties(null);

		// Assert
		fail();
	}

	@Test
	public void send() throws Exception {
		// Arrange
		NativeSession session = new NativeSession(bridge);
		TestObject testObject = new TestObject();

		// Act
		session.send(testObject);

		// Assert
		verify(bridge).send(eq(testObject));
	}

	@Test(expected = IllegalArgumentException.class)
	public void sendNull() throws Exception {
		// Arrange
		NativeSession session = new NativeSession(bridge);

		// Act
		session.send(null);

		// Assert
		fail();
	}

	@Test
	public void primed() throws Exception {
		// Arrange
		NativeSession session = new NativeSession(bridge);

		// Act
		Awaiting awaiting = session.primed();

		// Assert
		assertFalse(Synchronize.isEmpty(awaiting));
	}

	@Test
	public void newPrimation() throws Exception {
		// Arrange
		NativeSession session = new NativeSession(bridge);
		Awaiting awaiting = session.primed();
		session.triggerPrimed();

		// Act
		session.resetPrimed();

		// Assert
		assertSame(awaiting, session.primed());
	}

	private class TestObject {

	}
}