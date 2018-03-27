package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.Testing;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.CachePush;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@Testing(DistributorImpl.class)
public class DistributorImplTest {

	@Test(expected = IllegalArgumentException.class)
	public void toSpecificObjectNull() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toSpecific(objectToSend, (List<Predicate<Session>>) null);

		//Assert
	}

	@Test
	public void toSpecificMatchesOne() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toSpecific(objectToSend, s -> s.equals(session2), Session::isIdentified);

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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		List<Predicate<Session>> predicates = Collections.singletonList(s -> s.equals(session2));

		//Act
		distributor.toSpecific(null, predicates);

		//Assert
	}

	@Test(expected = NullPointerException.class)
	public void toSpecificArrayPredicatesEmpty() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toSpecific(objectToSend, (Predicate<Session>[]) null);

		//Assert
	}

	@Test
	public void toSpecificListMatchesOne() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		List<Predicate<Session>> predicates = Collections.singletonList(s -> s.equals(session2));

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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		List<Predicate<Session>> predicates = Collections.singletonList(s -> s.equals(session2) || s.equals(session3));

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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		List<Predicate<Session>> predicates = Arrays.asList(s -> s.equals(session2), Session::isIdentified);

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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		List<Predicate<Session>> predicates = Collections.singletonList(Session::isIdentified);

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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toAllExcept(objectToSend);

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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toAllExcept(objectToSend, s -> s.equals(session2), Session::isIdentified);

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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		List<Predicate<Session>> predicates = Collections.singletonList(s -> s.equals(session2));

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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		List<Predicate<Session>> predicates = Collections.emptyList();

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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();
		List<Predicate<Session>> predicates = Arrays.asList(s -> s.equals(session2), Session::isIdentified);

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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		when(clientList.sessionStream()).thenReturn(Stream.of(session1, session2, session3));
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toAllIdentified(objectToSend, s -> s.equals(session2), Session::isIdentified);

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
		ClientList clientList = mock(ClientList.class);
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
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
		ClientList clientList = mock(ClientList.class);
		when(clientList.sessionStream()).thenReturn(Stream.of(session4));
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		when(distributorRegistration.getRegistered(ObjectToSend.class)).thenReturn(Arrays.asList(session1, session2, session3));
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toRegistered(objectToSend, s -> s.equals(session2));

		//Assert
		verify(session1, never()).send(isA(CachePush.class));
		verify(session2).send(isA(CachePush.class));
		verify(session3, never()).send(isA(CachePush.class));
		verify(session4, never()).send(isA(CachePush.class));
	}

	@Test
	public void toRegisteredMultiplePredicates() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		Session session4 = mock(Session.class);
		when(session1.isIdentified()).thenReturn(false);
		when(session2.isIdentified()).thenReturn(true);
		when(session3.isIdentified()).thenReturn(false);
		when(session4.isIdentified()).thenReturn(true);
		ClientList clientList = mock(ClientList.class);
		when(clientList.sessionStream()).thenReturn(Stream.of(session4));
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		when(distributorRegistration.getRegistered(ObjectToSend.class)).thenReturn(Arrays.asList(session1, session2, session3));
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toRegistered(objectToSend, s -> s.equals(session2), Session::isIdentified);

		//Assert
		verify(session1, never()).send(isA(CachePush.class));
		verify(session2).send(isA(CachePush.class));
		verify(session3, never()).send(isA(CachePush.class));
		verify(session4, never()).send(isA(CachePush.class));
	}

	@Test
	public void toRegisteredNoPredicate() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		Session session4 = mock(Session.class);
		ClientList clientList = mock(ClientList.class);
		when(clientList.sessionStream()).thenReturn(Stream.of(session4));
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		when(distributorRegistration.getRegistered(ObjectToSend.class)).thenReturn(Arrays.asList(session1, session2, session3));
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		ObjectToSend objectToSend = new ObjectToSend();

		//Act
		distributor.toRegistered(objectToSend);

		//Assert
		verify(session1).send(isA(CachePush.class));
		verify(session2).send(isA(CachePush.class));
		verify(session3).send(isA(CachePush.class));
		verify(session4, never()).send(isA(CachePush.class));
	}

	@Test
	public void toRegisteredListOnePredicate() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		Session session4 = mock(Session.class);
		ClientList clientList = mock(ClientList.class);
		when(clientList.sessionStream()).thenReturn(Stream.of(session4));
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		when(distributorRegistration.getRegistered(ObjectToSend.class)).thenReturn(Arrays.asList(session1, session2, session3));
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		ObjectToSend objectToSend = new ObjectToSend();
		List<Predicate<Session>> predicates = Collections.singletonList(s -> s.equals(session2));

		//Act
		distributor.toRegistered(objectToSend, predicates);

		//Assert
		verify(session1, never()).send(isA(CachePush.class));
		verify(session2).send(isA(CachePush.class));
		verify(session3, never()).send(isA(CachePush.class));
		verify(session4, never()).send(isA(CachePush.class));
	}

	@Test
	public void toRegisteredListMultiplePredicates() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		Session session4 = mock(Session.class);
		when(session1.isIdentified()).thenReturn(false);
		when(session2.isIdentified()).thenReturn(true);
		when(session3.isIdentified()).thenReturn(false);
		when(session4.isIdentified()).thenReturn(true);
		ClientList clientList = mock(ClientList.class);
		when(clientList.sessionStream()).thenReturn(Stream.of(session4));
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		when(distributorRegistration.getRegistered(ObjectToSend.class)).thenReturn(Arrays.asList(session1, session2, session3));
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		ObjectToSend objectToSend = new ObjectToSend();
		List<Predicate<Session>> predicates = Arrays.asList(s -> s.equals(session2), Session::isIdentified);

		//Act
		distributor.toRegistered(objectToSend, predicates);

		//Assert
		verify(session1, never()).send(isA(CachePush.class));
		verify(session2).send(isA(CachePush.class));
		verify(session3, never()).send(isA(CachePush.class));
		verify(session4, never()).send(isA(CachePush.class));
	}

	@Test
	public void toRegisteredListNoPredicate() throws Exception {
		//Arrange
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		Session session4 = mock(Session.class);
		ClientList clientList = mock(ClientList.class);
		when(clientList.sessionStream()).thenReturn(Stream.of(session4));
		DistributorRegistration distributorRegistration = mock(DistributorRegistration.class);
		when(distributorRegistration.getRegistered(ObjectToSend.class)).thenReturn(Arrays.asList(session1, session2, session3));
		DistributorImpl distributor = new DistributorImpl(clientList, distributorRegistration);
		ObjectToSend objectToSend = new ObjectToSend();
		List<Predicate<Session>> predicates = Collections.emptyList();

		//Act
		distributor.toRegistered(objectToSend, predicates);

		//Assert
		verify(session1).send(isA(CachePush.class));
		verify(session2).send(isA(CachePush.class));
		verify(session3).send(isA(CachePush.class));
		verify(session4, never()).send(isA(CachePush.class));
	}

	private class ObjectToSend {

	}

}