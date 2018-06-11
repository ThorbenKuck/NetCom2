package com.github.thorbenkuck.netcom2.integration.chat.client;

import com.github.thorbenkuck.netcom2.integration.chat.common.Message;
import com.github.thorbenkuck.netcom2.integration.chat.common.User;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;

public class Instantiate {

	private ClientStart clientStart;

	public Instantiate(ClientStart clientStart) {
		this.clientStart = clientStart;
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

	public final void resolve() {
		clientHandlers();
		communication();
	}

}
