package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@Testing(NativeDistributor.class)
public class DistributorImplTest {

	private ServerStart serverStart;
	private ClientList clientList;

	@Before
	public void beforeEachTest() {
		CommunicationRegistration registration = mock(CommunicationRegistration.class);
		serverStart = mock(ServerStart.class);
		clientList = mock(ClientList.class);
		when(serverStart.getCommunicationRegistration()).thenReturn(registration);
		when(registration.register(any())).thenReturn(mock(ReceivePipeline.class));
		when(serverStart.clientList()).thenReturn(clientList);
		when(serverStart.cache()).thenReturn(mock(Cache.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void toSpecificObjectNull() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));

		//Act
		distributor.toSpecific(null, s -> s.equals(session2));

		//Assert
	}

	@Test(expected = IllegalArgumentException.class)
	public void toSpecificPredicatesEmpty() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toSpecific(objectToSend, null);

		//Assert
	}

	@Test
	public void toSpecificMatchesOne() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toSpecific(objectToSend, s -> s.equals(session2));

		//Assert
		verify(session1, never()).send(eq(objectToSend));
		verify(session2).send(eq(objectToSend));
		verify(session3, never()).send(eq(objectToSend));
	}

	@Test
	public void toSpecificMatchesMultiple() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toSpecific(objectToSend, s -> s.equals(session2) || s.equals(session3));

		//Assert
		verify(session1, never()).send(eq(objectToSend));
		verify(session2).send(eq(objectToSend));
		verify(session3).send(eq(objectToSend));
	}

	@Test
	public void toSpecificMatchesOneMultiplePredicates() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		when(session1.isIdentified()).thenReturn(false);
		when(session2.isIdentified()).thenReturn(true);
		when(session3.isIdentified()).thenReturn(true);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toSpecific(objectToSend, s -> s.equals(session2) && s.isIdentified());

		//Assert
		verify(session1, never()).send(eq(objectToSend));
		verify(session2).send(eq(objectToSend));
		verify(session3, never()).send(eq(objectToSend));
	}

	@Test
	public void toSpecificMatchesNone() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		when(session1.isIdentified()).thenReturn(false);
		when(session2.isIdentified()).thenReturn(false);
		when(session3.isIdentified()).thenReturn(false);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toSpecific(objectToSend, Session::isIdentified);

		//Assert
		verify(session1, never()).send(eq(objectToSend));
		verify(session2, never()).send(eq(objectToSend));
		verify(session3, never()).send(eq(objectToSend));
	}


	@Test(expected = IllegalArgumentException.class)
	public void toSpecificListObjectNull() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		Predicate<Session> predicates = s -> s.equals(session2);

		//Act
		distributor.toSpecific(null, predicates);

		//Assert
	}

	@Test
	public void toSpecificListMatchesOne() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		Predicate<Session> predicates = s -> s.equals(session2);

		//Act
		distributor.toSpecific(objectToSend, predicates);

		//Assert
		verify(session1, never()).send(eq(objectToSend));
		verify(session2).send(eq(objectToSend));
		verify(session3, never()).send(eq(objectToSend));
	}

	@Test
	public void toSpecificListMatchesMultiple() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		Predicate<Session> predicates = s -> s.equals(session2) || s.equals(session3);

		//Act
		distributor.toSpecific(objectToSend, predicates);

		//Assert
		verify(session1, never()).send(eq(objectToSend));
		verify(session2).send(eq(objectToSend));
		verify(session3).send(eq(objectToSend));
	}

	@Test
	public void toSpecificListMatchesOneMultiplePredicates() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		when(session1.isIdentified()).thenReturn(false);
		when(session2.isIdentified()).thenReturn(true);
		when(session3.isIdentified()).thenReturn(true);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		Predicate<Session> predicates = s -> s.equals(session2) && s.isIdentified();

		//Act
		distributor.toSpecific(objectToSend, predicates);

		//Assert
		verify(session1, never()).send(eq(objectToSend));
		verify(session2).send(eq(objectToSend));
		verify(session3, never()).send(eq(objectToSend));
	}

	@Test
	public void toSpecificListMatchesNone() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		when(session1.isIdentified()).thenReturn(false);
		when(session2.isIdentified()).thenReturn(false);
		when(session3.isIdentified()).thenReturn(false);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		Predicate<Session> predicates = Session::isIdentified;

		//Act
		distributor.toSpecific(objectToSend, predicates);

		//Assert
		verify(session1, never()).send(eq(objectToSend));
		verify(session2, never()).send(eq(objectToSend));
		verify(session3, never()).send(eq(objectToSend));
	}


	@Test
	public void toSpecificList() throws Exception {
	}

	@Test
	public void toAll() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toAll(objectToSend);

		//Assert
		verify(session1).send(eq(objectToSend));
		verify(session2).send(eq(objectToSend));
		verify(session3).send(eq(objectToSend));
	}

	@Test
	public void toAllExcept() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toAllExcept(objectToSend, s -> s.equals(session2));

		//Assert
		verify(session1).send(eq(objectToSend));
		verify(session2, never()).send(eq(objectToSend));
		verify(session3).send(eq(objectToSend));
	}

	@Test
	public void toAllExceptNoPredicates() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toAllExcept(objectToSend, s -> false);

		//Assert
		verify(session1).send(eq(objectToSend));
		verify(session2).send(eq(objectToSend));
		verify(session3).send(eq(objectToSend));
	}

	@Test
	public void toAllExceptMultiplePredicates() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		when(session1.isIdentified()).thenReturn(false);
		when(session2.isIdentified()).thenReturn(false);
		when(session3.isIdentified()).thenReturn(true);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toAllExcept(objectToSend, s -> s.equals(session2) || s.isIdentified());

		//Assert
		verify(session1).send(eq(objectToSend));
		verify(session2, never()).send(eq(objectToSend));
		verify(session3, never()).send(eq(objectToSend));
	}

	@Test
	public void toAllExceptList() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		Predicate<Session> predicates = s -> s.equals(session2);

		//Act
		distributor.toAllExcept(objectToSend, predicates);

		//Assert
		verify(session1).send(eq(objectToSend));
		verify(session2, never()).send(eq(objectToSend));
		verify(session3).send(eq(objectToSend));
	}

	@Test
	public void toAllExceptListNoPredicates() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		Predicate<Session> predicates = s -> false;

		//Act
		distributor.toAllExcept(objectToSend, predicates);

		//Assert
		verify(session1).send(eq(objectToSend));
		verify(session2).send(eq(objectToSend));
		verify(session3).send(eq(objectToSend));
	}

	@Test
	public void toAllExceptListMultiplePredicates() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		when(session1.isIdentified()).thenReturn(false);
		when(session2.isIdentified()).thenReturn(false);
		when(session3.isIdentified()).thenReturn(true);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		Predicate<Session> predicates = s -> s.isIdentified() || s.equals(session2);

		//Act
		distributor.toAllExcept(objectToSend, predicates);

		//Assert
		verify(session1).send(eq(objectToSend));
		verify(session2, never()).send(eq(objectToSend));
		verify(session3, never()).send(eq(objectToSend));
	}

	@Test
	public void toAllIdentifiedMultiplePredicates() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		when(session1.isIdentified()).thenReturn(false);
		when(session2.isIdentified()).thenReturn(true);
		when(session3.isIdentified()).thenReturn(true);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toAllIdentified(objectToSend, s -> s.equals(session2) && s.isIdentified());

		//Assert
		verify(session1, never()).send(eq(objectToSend));
		verify(session2).send(eq(objectToSend));
		verify(session3, never()).send(eq(objectToSend));
	}

	@Test
	public void toAllIdentifiedNoPredicates() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		when(session1.isIdentified()).thenReturn(false);
		when(session2.isIdentified()).thenReturn(true);
		when(session3.isIdentified()).thenReturn(true);

		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toAllIdentified(objectToSend);

		//Assert
		verify(session1, never()).send(eq(objectToSend));
		verify(session2).send(eq(objectToSend));
		verify(session3).send(eq(objectToSend));
	}

	@Test
	public void toRegisteredOnePredicate() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		Session session4 = mock(Session.class);

		when(clientList.sessionStream()).thenReturn(Stream.of(session4));
		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toRegistered(objectToSend);

		//Assert
		verify(session1, never()).send(any());
		verify(session2, never()).send(any());
		verify(session3, never()).send(any());
		verify(session4, never()).send(any());
	}

	private class ObjectToSend {

	}

}