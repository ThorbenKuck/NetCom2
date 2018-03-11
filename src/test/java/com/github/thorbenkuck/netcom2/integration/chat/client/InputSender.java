package com.github.thorbenkuck.netcom2.integration.chat.client;

import com.github.thorbenkuck.netcom2.integration.chat.common.Message;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.integration.chat.common.Logout;

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
		if (input.equals("logout")) {
			sender.objectToServer(new Logout());
			System.out.println("LoggingExample out ..");
			running = false;
		} else if (input.equals("help")) {
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