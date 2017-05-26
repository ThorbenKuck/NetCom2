package test.examples.chat.server;

import de.thorbenkuck.netcom2.network.server.ServerStart;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import test.examples.chat.common.Login;
import test.examples.chat.common.Logout;
import test.examples.chat.common.Message;
import test.examples.chat.common.User;

import java.time.LocalDateTime;

public class Instantiate {

	private final ServerStart serverStart;
	private final UserList userList;

	public Instantiate(ServerStart serverStart, UserList userList) {
		this.serverStart = serverStart;
		this.userList = userList;
	}

	public final void resolve() {
		clientHandlers();
		communication();
	}

	private void clientHandlers() {
		serverStart.addClientConnectedHandler(client -> {
			Session session = client.getSession();
			userList.add(session, new User());
			client.addDisconnectedHandler(client1 -> userList.remove(session));
		});
	}

	private void communication() {
		CommunicationRegistration communicationRegistration = serverStart.getCommunicationRegistration();

		communicationRegistration.register(Login.class)
				.addFirst((session, login) -> {
					session.setIdentified(true);
					User user = userList.get(session);
					user.setUserName(login.getUserName());
					session.send(user);
				});

		communicationRegistration.register(Logout.class)
				.addFirst((session, login) -> session.setIdentified(false))
				.withRequirement(Session::isIdentified);

		// #############

		communicationRegistration.register(Message.class)
				.addFirst((session, message) -> {
					Message toSend = new Message(" [" + LocalDateTime.now() + "]: " + message.getMessage(), message.getSender());
					serverStart.distribute().toAllIdentified(toSend);
				})
				.withRequirement(Session::isIdentified);

		communicationRegistration.register(Message.class)
				.addLast((session, message) -> {
					System.out.println("Session is unidentified: " + ! session.isIdentified());
					session.send(new Message("! You have to be logged in, to send a message !", new User()));
				})
				.withRequirement(session -> ! session.isIdentified());
	}
}
