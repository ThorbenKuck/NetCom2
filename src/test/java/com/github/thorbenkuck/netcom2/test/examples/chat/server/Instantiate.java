package com.github.thorbenkuck.netcom2.test.examples.chat.server;

import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.test.examples.chat.common.Login;
import com.github.thorbenkuck.netcom2.test.examples.chat.common.Logout;
import com.github.thorbenkuck.netcom2.test.examples.chat.common.Message;
import com.github.thorbenkuck.netcom2.test.examples.chat.common.User;

import java.time.LocalDateTime;

public class Instantiate {

	private final ServerStart serverStart;
	private final UserList userList;
	private final User systemUser = new User("[System]: ");

	public Instantiate(ServerStart serverStart, UserList userList) {
		this.serverStart = serverStart;
		this.userList = userList;
	}

	private void clientHandlers() {
		serverStart.addClientConnectedHandler(client -> {
			Session session = client.getSession();
			userList.add(session, new User());
			client.addDisconnectedHandler(client1 -> {
				User user = userList.remove(session);
				serverStart.distribute().toAllIdentified(new Message("User: " + user.getUserName() + " disconnected!", systemUser));
			});
		});
	}

	private void communication() {
		CommunicationRegistration communicationRegistration = serverStart.getCommunicationRegistration();

		communicationRegistration.register(Login.class)
				.addFirst((session, login) -> {
					System.out.println("received Login-request.. logging in!");
					User user = userList.get(session);
					user.setUserName(login.getUserName());
					session.send(user);
					serverStart.distribute().toAllIdentified(new Message("User " + user.getUserName() + " connected!", systemUser));
					session.setIdentified(true);
				}).withRequirement(session -> ! session.isIdentified());

		communicationRegistration.register(Logout.class)
				.addFirst((session, login) -> session.setIdentified(false))
				.withRequirement(Session::isIdentified);

		// #############

		communicationRegistration.register(Message.class)
				.addFirst((session, message) -> {
					Message toSend = new Message(" [" + LocalDateTime.now() + "]: " + message.getMessage(), message.getSender());
					serverStart.distribute()
							.toAllIdentified(toSend);
				}).withRequirement(Session::isIdentified);

		communicationRegistration.register(Message.class)
				.addLast((session, message) -> {
					System.out.println("Session is unidentified: " + ! session.isIdentified());
					session.send(new Message("! You have to be logged in, to send a message !", new User()));
				}).withRequirement(session -> ! session.isIdentified());
	}

	public final void resolve() {
		clientHandlers();
		communication();
	}
}
