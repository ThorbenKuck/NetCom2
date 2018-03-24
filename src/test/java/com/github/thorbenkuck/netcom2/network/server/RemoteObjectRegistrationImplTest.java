package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.rmi.RegistrationOverrideProhibited;
import com.github.thorbenkuck.netcom2.exceptions.RemoteObjectInvalidMethodException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationResponse;
import org.junit.Test;

import java.math.BigInteger;
import java.util.UUID;

import static com.github.thorbenkuck.netcom2.TestUtils.UUID_SEED_1;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RemoteObjectRegistrationImplTest {
	@Test(expected=IllegalArgumentException.class)
	public void registerInvalidArrayLength() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObject remoteObject = new TestRemoteObject();
		Class[] classes = new Class[0];

		//Act
		remoteObjectRegistration.register(remoteObject, classes);

		//Assert
	}

	@Test
	public void registerNotAssignable() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObject remoteObject = spy(new TestRemoteObject());
		Class<?> classToAssign = TestUnassignableRemoteObject.class;
		UUID uuid = UUID.fromString(UUID_SEED_1);
		String methodName = "aMethod";
		Object[] parameters = null;
		RemoteAccessCommunicationRequest communicationRequest = new RemoteAccessCommunicationRequest(methodName, classToAssign, uuid, parameters);

		//Act
		remoteObjectRegistration.register(remoteObject, classToAssign);
		remoteObjectRegistration.run(communicationRequest);

		//Assert
		verify(remoteObject, never()).aMethod();
	}

	@Test
	public void registerOverrideProhibited() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObject remoteObject = spy(new TestRemoteObject());
		Class<?> classToAssign = TestRegistrationOverrideProhibitedRemoteObject.class;
		String methodName = "aMethod";
		UUID uuid = UUID.fromString(UUID_SEED_1);
		Object[] parameters = null;
		RemoteAccessCommunicationRequest communicationRequest = new RemoteAccessCommunicationRequest(methodName, classToAssign, uuid, parameters);

		//Act
		remoteObjectRegistration.register(remoteObject, classToAssign);
		remoteObjectRegistration.run(communicationRequest);

		//Assert
		verify(remoteObject, never()).aMethod();
	}

	@Test
	public void registerSuccess() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObject remoteObject = spy(new TestRemoteObject());
		Class<?> classToAssign = TestRemoteObject.class;
		String methodName = "aMethod";
		UUID uuid = UUID.fromString(UUID_SEED_1);
		Object[] parameters = null;
		RemoteAccessCommunicationRequest communicationRequest = new RemoteAccessCommunicationRequest(methodName, classToAssign, uuid, parameters);

		//Act
		remoteObjectRegistration.register(remoteObject, classToAssign);
		remoteObjectRegistration.run(communicationRequest);

		//Assert
		verify(remoteObject).aMethod();
	}

	@Test
	public void hookInterface() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObjectWithInterface object = spy(new TestRemoteObjectWithInterface());
		String methodName = "anInterfaceMethod";
		Class<TestRemoteObjectWithInterface> clazz = TestRemoteObjectWithInterface.class;
		Object[] parameters = null;
		UUID uuid = UUID.fromString(UUID_SEED_1);
		RemoteAccessCommunicationRequest request = new RemoteAccessCommunicationRequest(methodName, clazz, uuid, parameters);

		//Act
		remoteObjectRegistration.hook(object);
		remoteObjectRegistration.run(request);

		//Assert
		verify(object).anInterfaceMethod();
	}

	@Test(expected = IllegalArgumentException.class)
	public void hookNull() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();

		//Act
		remoteObjectRegistration.hook(null);

		//Assert
	}

	@Test
	public void unregisterObjectWithClassesSuccess() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObject object = spy(new TestRemoteObject());
		String methodName = "aMethod";
		Class<TestRemoteObject> clazz = TestRemoteObject.class;
		Object[] parameters = null;
		UUID uuid = UUID.fromString(UUID_SEED_1);
		RemoteAccessCommunicationRequest request = new RemoteAccessCommunicationRequest(methodName, clazz, uuid, parameters);

		//Act
		remoteObjectRegistration.register(object, clazz);
		remoteObjectRegistration.unregister(object, clazz);
		remoteObjectRegistration.run(request);

		//Assert
		verify(object, never()).aMethod();
	}

	@Test
	public void unregisterObjectWithClassesFail() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObject object = spy(new TestRemoteObject());
		String methodName = "aMethod";
		Class<TestRemoteObject> clazz = TestRemoteObject.class;
		Object[] parameters = null;
		UUID uuid = UUID.fromString(UUID_SEED_1);
		RemoteAccessCommunicationRequest request = new RemoteAccessCommunicationRequest(methodName, clazz, uuid, parameters);

		//Act
		remoteObjectRegistration.register(object, clazz);
		remoteObjectRegistration.unregister(object, AnInterface.class);
		remoteObjectRegistration.run(request);

		//Assert
		verify(object).aMethod();
	}

	@Test
	public void unregisterNoObjectOnlyClasses() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObjectWithInterface object = spy(new TestRemoteObjectWithInterface());
		String methodName = "anInterfaceMethod";
		Class<TestRemoteObjectWithInterface> clazz = TestRemoteObjectWithInterface.class;
		Object[] parameters = null;
		UUID uuid = UUID.fromString(UUID_SEED_1);
		RemoteAccessCommunicationRequest request = new RemoteAccessCommunicationRequest(methodName, clazz, uuid, parameters);
		RemoteAccessCommunicationRequest request2 = new RemoteAccessCommunicationRequest(methodName, AnInterface.class, uuid, parameters);

		//Act
		remoteObjectRegistration.hook(object);
		remoteObjectRegistration.unregister(clazz, AnInterface.class);
		remoteObjectRegistration.run(request);
		remoteObjectRegistration.run(request2);

		//Assert
		verify(object, never()).anInterfaceMethod();
	}

	@Test
	public void unhook() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObjectWithInterface object = spy(new TestRemoteObjectWithInterface());
		String methodName = "anInterfaceMethod";
		String methodName2 = "aMethod";
		Class<TestRemoteObjectWithInterface> clazz = TestRemoteObjectWithInterface.class;
		Object[] parameters = null;
		UUID uuid = UUID.fromString(UUID_SEED_1);
		RemoteAccessCommunicationRequest request = new RemoteAccessCommunicationRequest(methodName, clazz, uuid, parameters);
		RemoteAccessCommunicationRequest request2 = new RemoteAccessCommunicationRequest(methodName2, clazz, uuid, parameters);

		//Act
		remoteObjectRegistration.hook(object);
		remoteObjectRegistration.unhook(object);
		remoteObjectRegistration.run(request);
		remoteObjectRegistration.run(request2);

		//Assert
		verify(object, never()).anInterfaceMethod();
		verify(object, never()).aMethod();
	}

	@Test
	public void clear() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObject remoteObject = spy(new TestRemoteObject());
		Class<?> classToAssign = TestRemoteObject.class;
		String methodName = "aMethod";
		UUID uuid = UUID.fromString(UUID_SEED_1);
		Object[] parameters = null;
		RemoteAccessCommunicationRequest communicationRequest = new RemoteAccessCommunicationRequest(methodName, classToAssign, uuid, parameters);

		//Act
		remoteObjectRegistration.register(remoteObject, classToAssign);
		remoteObjectRegistration.clear();
		remoteObjectRegistration.run(communicationRequest);

		//Assert
		verify(remoteObject, never()).aMethod();
	}

	@Test
	public void runExistingMethod() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObject remoteObject = spy(new TestRemoteObject());
		Class<?> classToAssign = TestRemoteObject.class;
		String methodName = "aMethod";
		UUID uuid = UUID.fromString(UUID_SEED_1);
		Object[] parameters = null;
		RemoteAccessCommunicationRequest communicationRequest = new RemoteAccessCommunicationRequest(methodName, classToAssign, uuid, parameters);

		//Act
		remoteObjectRegistration.register(remoteObject, classToAssign);
		RemoteAccessCommunicationResponse response = remoteObjectRegistration.run(communicationRequest);

		//Assert
		verify(remoteObject).aMethod();
		assertNotNull(response);
		assertNull(response.getResult());
		assertEquals(uuid, response.getUuid());
		assertNull(response.getThrownThrowable());
	}

	@Test
	public void runExistingMethodWithParameters() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObject remoteObject = spy(new TestRemoteObject());
		Class<?> classToAssign = TestRemoteObject.class;
		String methodName = "aBigIntMethod";
		UUID uuid = UUID.fromString(UUID_SEED_1);
		BigInteger inputParam = new BigInteger("3");
		Object[] parameters = new Object[] {inputParam};
		RemoteAccessCommunicationRequest communicationRequest = new RemoteAccessCommunicationRequest(methodName, classToAssign, uuid, parameters);

		//Act
		remoteObjectRegistration.register(remoteObject, classToAssign);
		RemoteAccessCommunicationResponse response = remoteObjectRegistration.run(communicationRequest);

		//Assert
		verify(remoteObject).aBigIntMethod(eq(inputParam));
		assertNotNull(response);
		assertNotNull(response.getResult());
		assertEquals(new BigInteger("4"), response.getResult());
		assertEquals(uuid, response.getUuid());
		assertNull(response.getThrownThrowable());
	}

	@Test
	public void runNonExistentNameMethod() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObject remoteObject = spy(new TestRemoteObject());
		Class<?> classToAssign = TestRemoteObject.class;
		String methodName = "aNonExistentMethod";
		UUID uuid = UUID.fromString(UUID_SEED_1);
		Object[] parameters = null;
		RemoteAccessCommunicationRequest communicationRequest = new RemoteAccessCommunicationRequest(methodName, classToAssign, uuid, parameters);

		//Act
		remoteObjectRegistration.register(remoteObject, classToAssign);
		RemoteAccessCommunicationResponse response = remoteObjectRegistration.run(communicationRequest);

		//Assert
		verify(remoteObject, never()).aMethod();
		assertNotNull(response);
		assertNull(response.getResult());
		assertEquals(uuid, response.getUuid());
		assertNotNull(response.getThrownThrowable());
		assertThat(response.getThrownThrowable(), instanceOf(RemoteObjectInvalidMethodException.class));
	}

	@Test
	public void runNonExistentParameterMethod() throws Exception {
		//Arrange
		RemoteObjectRegistrationImpl remoteObjectRegistration = new RemoteObjectRegistrationImpl();
		TestRemoteObject remoteObject = spy(new TestRemoteObject());
		Class<?> classToAssign = TestRemoteObject.class;
		String methodName = "aNonExistentMethod";
		UUID uuid = UUID.fromString(UUID_SEED_1);
		Object[] parameters = new Object[] {new BigInteger("3")};
		RemoteAccessCommunicationRequest communicationRequest = new RemoteAccessCommunicationRequest(methodName, classToAssign, uuid, parameters);

		//Act
		remoteObjectRegistration.register(remoteObject, classToAssign);
		RemoteAccessCommunicationResponse response = remoteObjectRegistration.run(communicationRequest);

		//Assert
		verify(remoteObject, never()).aMethod();
		assertNotNull(response);
		assertNull(response.getResult());
		assertEquals(uuid, response.getUuid());
		assertNotNull(response.getThrownThrowable());
		assertThat(response.getThrownThrowable(), instanceOf(RemoteObjectInvalidMethodException.class));
	}

	private class TestRemoteObject {

		public void aMethod() {}

		public BigInteger aBigIntMethod(final BigInteger bigInteger) {
			return bigInteger.add(BigInteger.ONE);
		}

	}

	private class TestRemoteObjectWithInterface implements AnInterface {

		public void aMethod() {}

		@Override
		public void anInterfaceMethod() {

		}
	}

	private class TestUnassignableRemoteObject {

	}

	@RegistrationOverrideProhibited
	private class TestRegistrationOverrideProhibitedRemoteObject {

	}

	private interface AnInterface {

		void anInterfaceMethod();

	}

}