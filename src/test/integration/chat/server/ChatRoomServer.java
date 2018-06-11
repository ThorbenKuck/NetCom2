package com.github.thorbenkuck.netcom2.integration.chat.server;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

public class ChatRoomServer {

	private static ServerStart serverStart;
	private static UserList userList = new UserList();

	public static void main(String[] args) {
		NetComLogging.setLogging(Logging.getDefault());
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
