package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.Testing;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.thorbenkuck.netcom2.TestUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@Testing(NativeClientList.class)
public class ClientListImplTest {

	@Test
	public void addOnceOpen() throws Exception {
		//Arrange
		NativeClientList clients = new NativeClientList();
		Client clientToAdd = mock(Client.class);
		ClientID clientID = ClientID.fromString(UUID_SEED_1);
		when(clientToAdd.getId()).thenReturn(clientID);
		List<Client> expectedStreamContents = Collections.singletonList(clientToAdd);

		//Act
		clients.add(clientToAdd);
		Stream<Client> clientStream = clients.stream();
		List<Client> clientStreamContents = clientStream.collect(Collectors.toList());

		//Assert
		assertThat(clientStreamContents, consistsOf(expectedStreamContents));
	}

	@Test
	public void addTwiceOpen() throws Exception {
		//Arrange
		NativeClientList clients = new NativeClientList();
		Client clientToAdd = mock(Client.class);
		when(clientToAdd.getId()).thenReturn(ClientID.fromString(UUID_SEED_1));
		List<Client> expectedStreamContents = Collections.singletonList(clientToAdd);

		//Act
		clients.add(clientToAdd);
		clients.add(clientToAdd);
		Stream<Client> clientStream = clients.stream();
		List<Client> clientStreamContents = clientStream.collect(Collectors.toList());

		//Assert
		assertThat(clientStreamContents, consistsOf(expectedStreamContents));
	}

	@Test
	public void addOnceClosed() throws Exception {
		//Arrange
		NativeClientList clients = new NativeClientList();
		Client clientToAdd = mock(Client.class);
		ClientID clientID = ClientID.fromString(UUID_SEED_1);
		when(clientToAdd.getId()).thenReturn(clientID);
		List<Client> expectedStreamContents = Collections.emptyList();

		//Act
		clients.close();
		clients.add(clientToAdd);
		Stream<Client> clientStream = clients.stream();
		List<Client> clientStreamContents = clientStream.collect(Collectors.toList());

		//Assert
		assertThat(clientStreamContents, consistsOf(expectedStreamContents));
	}

	@Test
	public void addTwiceClosed() throws Exception {
		//Arrange
		NativeClientList clients = new NativeClientList();
		Client clientToAdd = mock(Client.class);
		when(clientToAdd.getId()).thenReturn(ClientID.fromString(UUID_SEED_1));

		//Act
		clients.close();
		clients.add(clientToAdd);
		clients.add(clientToAdd);
		Stream<Client> clientStream = clients.stream();
		List<Client> clientStreamContents = clientStream.collect(Collectors.toList());

		//Assert
		assertTrue(clientStreamContents.isEmpty());
	}

	@Test
	public void removeOpenFound() throws Exception {
		//Arrange
		Client client1 = mock(Client.class);
		when(client1.getId()).thenReturn(ClientID.fromString(UUID_SEED_1));
		Client client2 = mock(Client.class);
		when(client2.getId()).thenReturn(ClientID.fromString(UUID_SEED_2));
		NativeClientList clients = new NativeClientList();
		List<Client> expectedStreamContents = Collections.singletonList(client1);

		//Act
		clients.add(client1);
		clients.add(client2);
		clients.remove(client2);
		Stream<Client> clientStream = clients.stream();
		List<Client> clientStreamContents = clientStream.collect(Collectors.toList());

		//Assert
		assertThat(clientStreamContents, consistsOf(expectedStreamContents));
	}

	@Test
	public void removeOpenNotFound() throws Exception {
		//Arrange
		Client client1 = mock(Client.class);
		when(client1.getId()).thenReturn(ClientID.fromString(UUID_SEED_1));
		Client client2 = mock(Client.class);
		when(client2.getId()).thenReturn(ClientID.fromString(UUID_SEED_2));
		Client client3 = mock(Client.class);
		NativeClientList clients = new NativeClientList();
		List<Client> expectedStreamContents = Arrays.asList(client1, client2);

		//Act
		clients.add(client1);
		clients.add(client2);
		clients.remove(client3);
		Stream<Client> clientStream = clients.stream();
		List<Client> clientStreamContents = clientStream.collect(Collectors.toList());

		//Assert
		assertThat(clientStreamContents, consistsOf(expectedStreamContents));
	}

	@Test
	public void removeClosedFound() throws Exception {
		//Arrange
		Client client1 = mock(Client.class);
		when(client1.getId()).thenReturn(ClientID.fromString(UUID_SEED_1));
		Client client2 = mock(Client.class);
		when(client2.getId()).thenReturn(ClientID.fromString(UUID_SEED_2));
		NativeClientList clients = new NativeClientList();
		List<Client> expectedStreamContents = Arrays.asList(client1, client2);

		//Act
		clients.add(client1);
		clients.add(client2);
		clients.close();
		clients.remove(client2);
		Stream<Client> clientStream = clients.stream();
		List<Client> clientStreamContents = clientStream.collect(Collectors.toList());

		//Assert
		assertThat(clientStreamContents, consistsOf(expectedStreamContents));
	}

	@Test
	public void removeClosedNotFound() throws Exception {
		//Arrange
		Client client1 = mock(Client.class);
		when(client1.getId()).thenReturn(ClientID.fromString(UUID_SEED_1));
		Client client2 = mock(Client.class);
		when(client2.getId()).thenReturn(ClientID.fromString(UUID_SEED_2));
		Client client3 = mock(Client.class);
		NativeClientList clients = new NativeClientList();
		List<Client> expectedStreamContents = Arrays.asList(client1, client2);

		//Act
		clients.add(client1);
		clients.add(client2);
		clients.close();
		clients.remove(client3);
		Stream<Client> clientStream = clients.stream();
		List<Client> clientStreamContents = clientStream.collect(Collectors.toList());

		//Assert
		assertThat(clientStreamContents, consistsOf(expectedStreamContents));
	}

	@Test
	public void clearOpen() throws Exception {
		//Arrange
		Client aClient = mock(Client.class);
		when(aClient.getId()).thenReturn(ClientID.fromString(UUID_SEED_1));
		NativeClientList clients = new NativeClientList();

		//Act
		clients.add(aClient);
		clients.clear();
		Stream<Client> clientStream = clients.stream();
		List<Client> clientStreamContents = clientStream.collect(Collectors.toList());

		//Assert
		assertTrue(clientStreamContents.isEmpty());
	}

	@Test
	public void clearClosed() throws Exception {
		//Arrange
		Client aClient = mock(Client.class);
		when(aClient.getId()).thenReturn(ClientID.fromString(UUID_SEED_1));
		NativeClientList clients = new NativeClientList();
		List<Client> expectedStreamContents = Collections.singletonList(aClient);

		//Act
		clients.add(aClient);
		clients.close();
		clients.clear();
		Stream<Client> clientStream = clients.stream();
		List<Client> clientStreamContents = clientStream.collect(Collectors.toList());

		//Assert
		assertThat(clientStreamContents, consistsOf(expectedStreamContents));
	}

	@Test
	public void getOneClientBySessionOpen() throws Exception {
		//Arrange
		Client client = mock(Client.class);
		Session clientSession = mock(Session.class);
		when(client.getSession()).thenReturn(clientSession);
		NativeClientList clients = new NativeClientList();

		//Act
		clients.add(client);
		Optional<Client> returnedClientOptional = clients.getClient(clientSession);

		//Assert
		assertTrue(returnedClientOptional.isPresent());
		returnedClientOptional.ifPresent(c -> assertEquals(client, c));
	}

	@Test
	public void getOneClientBySessionOpenNotFound() throws Exception {
		//Arrange
		Client client = mock(Client.class);
		Session clientSession = mock(Session.class);
		Session searchSession = mock(Session.class);
		when(client.getSession()).thenReturn(clientSession);
		NativeClientList clients = new NativeClientList();

		//Act
		clients.add(client);
		Optional<Client> returnedClientOptional = clients.getClient(searchSession);

		//Assert
		assertFalse(returnedClientOptional.isPresent());
	}

	@Test
	public void getMultipleClientBySessionOpen() throws Exception {
		//Arrange
		Client client = mock(Client.class);
		Session clientSession = mock(Session.class);
		when(client.getSession()).thenReturn(clientSession);
		when(client.getId()).thenReturn(ClientID.fromString(UUID_SEED_1));
		Client anotherClient = mock(Client.class);
		Session searchSession = mock(Session.class);
		when(anotherClient.getId()).thenReturn(ClientID.fromString(UUID_SEED_2));
		when(anotherClient.getSession()).thenReturn(searchSession);
		NativeClientList clients = new NativeClientList();

		//Act
		clients.add(client);
		clients.add(anotherClient);
		Optional<Client> returnedClientOptional = clients.getClient(searchSession);

		//Assert
		assertNotNull(returnedClientOptional);
		assertTrue(returnedClientOptional.isPresent());
		returnedClientOptional.ifPresent(c -> assertEquals(anotherClient, c));
	}

	@Test
	public void getOneClientBySessionClosed() throws Exception {
		//Arrange
		Client client = mock(Client.class);
		Session clientSession = mock(Session.class);
		when(client.getSession()).thenReturn(clientSession);
		NativeClientList clients = new NativeClientList();

		//Act
		clients.close();
		clients.add(client);
		Optional<Client> returnedClientOptional = clients.getClient(clientSession);

		//Assert
		assertFalse(returnedClientOptional.isPresent());
	}

	@Test
	public void getOneClientByIDOpen() throws Exception {
		//Arrange
		Client client = mock(Client.class);
		ClientID clientID = ClientID.fromString(UUID_SEED_1);
		when(client.getId()).thenReturn(clientID);
		NativeClientList clients = new NativeClientList();

		//Act
		clients.add(client);
		Optional<Client> returnedClientOptional = clients.getClient(clientID);

		//Assert
		assertTrue(returnedClientOptional.isPresent());
		returnedClientOptional.ifPresent(c -> assertEquals(client, c));
	}

	@Test
	public void getOneClientByIDOpenNotFound() throws Exception {
		//Arrange
		Client client = mock(Client.class);
		ClientID clientID = ClientID.fromString(UUID_SEED_1);
		ClientID searchID = ClientID.fromString(UUID_SEED_2);
		when(client.getId()).thenReturn(clientID);
		NativeClientList clients = new NativeClientList();

		//Act
		clients.add(client);
		Optional<Client> returnedClientOptional = clients.getClient(searchID);

		//Assert
		assertFalse(returnedClientOptional.isPresent());
	}

	@Test
	public void getMultipleClientByIDOpen() throws Exception {
		//Arrange
		Client client = mock(Client.class);
		ClientID anID = ClientID.fromString(UUID_SEED_1);
		when(client.getId()).thenReturn(anID);
		Client anotherClient = mock(Client.class);
		ClientID anotherID = ClientID.fromString(UUID_SEED_2);
		when(anotherClient.getId()).thenReturn(anotherID);
		NativeClientList clients = new NativeClientList();

		//Act
		clients.add(client);
		clients.add(anotherClient);
		Optional<Client> returnedClientOptional = clients.getClient(anotherID);

		//Assert
		assertNotNull(returnedClientOptional);
		assertTrue(returnedClientOptional.isPresent());
		returnedClientOptional.ifPresent(c -> assertEquals(anotherClient, c));
	}

	@Test
	public void getOneClientByIDClosed() throws Exception {
		//Arrange
		Client client = mock(Client.class);
		ClientID clientID = ClientID.fromString(UUID_SEED_1);
		when(client.getId()).thenReturn(clientID);
		NativeClientList clients = new NativeClientList();

		//Act
		clients.close();
		clients.add(client);
		Optional<Client> returnedClientOptional = clients.getClient(clientID);

		//Assert
		assertFalse(returnedClientOptional.isPresent());
	}

	@Test
	public void sessionStream() throws Exception {
		//Arrange
		Client client = mock(Client.class);
		when(client.getId()).thenReturn(ClientID.fromString(UUID_SEED_1));
		Session clientSession = mock(Session.class);
		when(client.getSession()).thenReturn(clientSession);
		Client client2 = mock(Client.class);
		when(client2.getId()).thenReturn(ClientID.fromString(UUID_SEED_2));
		Session session2 = mock(Session.class);
		when(client2.getSession()).thenReturn(session2);
		Client client3 = mock(Client.class);
		when(client3.getId()).thenReturn(ClientID.fromString(UUID_SEED_3));
		when(client3.getSession()).thenReturn(null);
		NativeClientList clients = new NativeClientList();
		List<Session> expectedSessions = Arrays.asList(clientSession, session2);

		//Act
		clients.add(client);
		clients.add(client2);
		clients.add(client3);
		Stream<Session> sessionStream = clients.sessionStream();
		List<Session> streamContents = sessionStream.collect(Collectors.toList());

		//Assert
		assertThat(streamContents, consistsOf(expectedSessions));
	}

	@Test
	public void close() throws Exception {
		//Arrange
		Client aClient = mock(Client.class);
		when(aClient.getId()).thenReturn(ClientID.fromString(UUID_SEED_1));
		Client anotherClient = mock(Client.class);
		when(anotherClient.getId()).thenReturn(ClientID.fromString(UUID_SEED_2));
		NativeClientList clients = new NativeClientList();

		//Act
		clients.add(aClient);
		clients.add(anotherClient);
		clients.close();

		//Assert
		assertFalse(clients.isOpen());
		verify(aClient).disconnect();
		verify(anotherClient).disconnect();
	}

	@Test
	public void open() throws Exception {
		//Arrange
		NativeClientList clients = new NativeClientList();

		//Act
		clients.open();

		//Assert
		assertTrue(clients.isOpen());
	}

	@Test
	public void isOpen() throws Exception {
		//Arrange
		NativeClientList clients = new NativeClientList();

		//Act

		//Assert
		assertTrue(clients.isOpen());
	}

}