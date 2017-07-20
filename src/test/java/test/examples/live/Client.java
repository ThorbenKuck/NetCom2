package test.examples.live;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.ClientStart;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;

public class Client {

	private ClientStart clientStart;

	public Client() {
		clientStart = ClientStart.at("localhost", 4444);
	}

	public static void main(String[] args) throws StartFailedException {
		new Client().start();
	}

	public void start() throws StartFailedException {
		register(clientStart);

		clientStart.launch();

		afterLaunch(clientStart);
	}

	private void register(ClientStart clientStart) {
		clientStart.getCommunicationRegistration()
				.register(TestObject.class)
				.addFirst(System.out::println);
	}

	private void afterLaunch(ClientStart clientStart) {
		clientStart.send()
				.objectToServer(new TestObject("this comes from the Default connection"));

		Awaiting newConnection = clientStart.createNewConnection(TestObject.class);

		System.out.println("Some work");
		System.out.println("Waiting for the Connection to finish");
		try {
			newConnection.synchronize();
			System.out.println("Connection established!");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		clientStart.send()
				.objectToServer(new TestObject("this comes from the TestObject connection"), TestObject.class);

		clientStart.send()
				.objectToServer(new TestObject("this comes from the Default connection"));

		clientStart.send()
				.objectToServer(new TestObject("this comes from the TestObject connection"), TestObject.class);
	}

}
