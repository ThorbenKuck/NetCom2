package test.examples.chat.client;

import de.thorbenkuck.netcom2.network.interfaces.ClientStart;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import test.examples.chat.common.Message;
import test.examples.chat.common.User;

public class Instantiate {

	private ClientStart clientStart;

	public Instantiate(ClientStart clientStart) {
		this.clientStart = clientStart;
	}

	public final void resolve() {
		clientHandlers();
		communication();
	}

	private void clientHandlers() {
		clientStart.addDisconnectedHandler(client -> System.out.println("disconnected from Server"));
	}

	private void communication() {
		CommunicationRegistration communicationRegistration = clientStart.getCommunicationRegistration();

		communicationRegistration.register(Message.class)
				.addLast((session, message) -> System.out.println(message.getSender() + message.getMessage()));

		communicationRegistration.register(User.class)
				.addFirst((session, user) -> ChatRoomClient.setUser(user));
	}

}