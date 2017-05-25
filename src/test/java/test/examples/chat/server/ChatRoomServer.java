package test.examples.chat.server;

import de.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.network.server.ServerStart;

public class ChatRoomServer {

	private static ServerStart serverStart;
	private static UserList userList = new UserList();

	public static void main(String[] args) {
		serverStart = ServerStart.at(8000);

		new Instantiate(serverStart, userList).resolve();

		try {
			serverStart.launch();
			serverStart.acceptAllNextClients();
		} catch (StartFailedException | ClientConnectionFailedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
