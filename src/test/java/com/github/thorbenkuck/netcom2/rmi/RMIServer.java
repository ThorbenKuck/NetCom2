package com.github.thorbenkuck.netcom2.rmi;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.server.RemoteObjectRegistration;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

public class RMIServer {

	public static final String SERVER_PREFIX = "[FROM_SERVER]: ";

	public static void main(String[] args) {
		ServerStart serverStart = ServerStart.at(4568);

		RemoteObjectRegistration remoteObjectRegistration = RemoteObjectRegistration.open(serverStart);
		remoteObjectRegistration.hook(new ServerImplementation());

		try {
			serverStart.launch();
			serverStart.acceptAllNextClients();
		} catch (StartFailedException | ClientConnectionFailedException e) {
			e.printStackTrace();
		}
	}


	private static final class ServerImplementation implements RemoteTestObject {

		@Override
		public String convert(String input) {
			return SERVER_PREFIX + input;
		}
	}

}
