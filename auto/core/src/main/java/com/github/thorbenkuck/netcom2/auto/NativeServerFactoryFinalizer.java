package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.NetworkInterfaceFactoryException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.UnhandledExceptionContainer;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.util.concurrent.ExecutorService;

public class NativeServerFactoryFinalizer implements ServerFactoryFinalizer {

	private final ServerStart serverStart;

	public NativeServerFactoryFinalizer(ServerStart serverStart) {
		this.serverStart = serverStart;
	}

	private void launch(ServerStart serverStart) {
		try {
			serverStart.launch();
		} catch (StartFailedException e) {
			throw new NetworkInterfaceFactoryException(e);
		}
	}

	@Override
	public void onCurrentThread() {
		launch(serverStart);

		try {
			serverStart.acceptAllNextClients();
		} catch (ClientConnectionFailedException e) {
			throw new NetworkInterfaceFactoryException(e);
		}
	}

	@Override
	public ServerStart on(ExecutorService executorService) {
		launch(serverStart);

		executorService.execute(new ServerStartAcceptor(serverStart));

		return serverStart;
	}

	@Override
	public ServerStart onNetComThreadPool() {
		launch(serverStart);

		NetComThreadPool.submitCustomProcess(new ServerStartAcceptor(serverStart));

		return serverStart;
	}

	@Override
	public ServerStart onThread() {
		launch(serverStart);

		new Thread(new ServerStartAcceptor(serverStart)).start();

		return serverStart;
	}

	private final class ServerStartAcceptor implements Runnable {

		private final ServerStart serverStart;

		private ServerStartAcceptor(ServerStart serverStart) {
			this.serverStart = serverStart;
		}

		@Override
		public void run() {
			while (serverStart.running()) {
				try {
					serverStart.acceptAllNextClients();
				} catch (ClientConnectionFailedException e) {
					UnhandledExceptionContainer.catching(e);
				}
			}
		}

		public ServerStart getServerStart() {
			return serverStart;
		}

		@Override
		public String toString() {
			return "ServerStartAcceptor{" +
					"serverStart=" + serverStart +
					'}';
		}
	}
}
