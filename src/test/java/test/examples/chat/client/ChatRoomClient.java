package test.examples.chat.client;

import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.ClientStart;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import test.examples.chat.common.Login;
import test.examples.chat.common.User;

import java.io.IOException;

public class ChatRoomClient {

	private static ClientStart clientStart;
	private static User user;

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.disabled());
		clientStart = ClientStart.at("localhost", 8000);
		UserInput userInput = new UserInput();

		try {
			clientStart.launch();
			new Instantiate(clientStart).resolve();
			System.out.print("Please enter your UserName: ");
			String userName = userInput.getNextLine();
			clientStart.send().objectToServer(new Login(userName)).andWaitForReceiving(User.class);
			new InputSender(userInput, clientStart.send()).magic();
		} catch (StartFailedException | IOException | InterruptedException e) {
			System.out.println("Error");
			e.printStackTrace(System.out);
			System.exit(1);
		}
		System.exit(0);
	}

	public static User getUser() {
		return ChatRoomClient.user;
	}

	public static void setUser(User user) {
		ChatRoomClient.user = user;
	}
}
