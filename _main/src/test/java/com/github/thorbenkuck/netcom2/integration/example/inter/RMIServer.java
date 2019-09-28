package com.github.thorbenkuck.netcom2.integration.example.inter;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.server.RemoteObjectRegistration;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RMIServer {

	private final ServerStart serverStart;
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	public RMIServer(int port) {
		this.serverStart = ServerStart.at(port);
	}

	public static void main(String[] args) {
		RMIServer rmiServer = new RMIServer(4444);
		rmiServer.run();
	}

	private void accept() {
		try {
			serverStart.acceptAllNextClients();
		} catch (ClientConnectionFailedException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		RemoteObjectRegistration remoteObjectRegistration = RemoteObjectRegistration.open(serverStart);
		remoteObjectRegistration.register(new ServerInternationalization(), Internationalization.class);

		try {
			serverStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
		}

		executorService.submit(this::accept);
	}

	private class ServerInternationalization implements Internationalization {

		ServerLanguageTable connector = new ServerLanguageTable();

		@Override
		public List<Language> getAvailableLanguages() {
			return connector.getAllAvailableLanguages();
		}

		@Override
		public List<String> getAvailableIdentifier() {
			return connector.getAvailableIdentifier();
		}

		@Override
		public String getInLanguage(String id, Language language) {
			return connector.lookUp(id, language);
		}
	}
}
