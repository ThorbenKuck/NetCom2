package com.github.thorbenkuck.netcom2.integration.chat.client;

import com.github.thorbenkuck.netcom2.integration.chat.common.Logout;
import com.github.thorbenkuck.netcom2.integration.chat.common.Message;
import com.github.thorbenkuck.netcom2.network.client.Sender;

import java.io.IOException;

public class InputSender {

	private UserInput userInput;
	private Sender sender;
	private boolean running = false;

	public InputSender(UserInput userInput, Sender sender) {
		this.userInput = userInput;
		this.sender = sender;
	}

	private void parse(String input) {
		// This is ugly, but only an example!
		if ("logout".equals(input)) {
			sender.objectToServer(new Logout());
			System.out.println("LoggingExample out ..");
			running = false;
		} else if ("help".equals(input)) {
			printHelp();
		} else {
			sender.objectToServer(new Message(input, ChatRoomClient.getUser()));
		}
	}

	private void printHelp() {
		System.out.println("----\nHELP\nType logout to close this application\nType help for help");
	}

	public void magic() {
		running = true;
		System.out.print("\n\n\n\n\n\n\n\n\n\nEnter your message to send it to the Server\n\n");
		while (running) {
			try {
				String input = userInput.getNextLine();
				parse(input);
			} catch (IOException e) {
				e.printStackTrace(System.out);
				running = false;
			}
		}
		System.out.println("Shutting application down");
	}
}
