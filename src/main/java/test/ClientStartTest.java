package test;

import de.thorbenkuck.netcom2.exceptions.CommunicationAlreadySpecifiedException;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.network.interfaces.ClientStart;
import de.thorbenkuck.netcom2.network.shared.cache.DeletedEntryEvent;
import de.thorbenkuck.netcom2.network.shared.cache.NewEntryEvent;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

public class ClientStartTest {
	private static ClientStart clientStart;
	private static int port = 46091;


	public static void main(String[] args) {
		clientStart = ClientStart.of("localhost", port);

		try {
			register();
			start();
			clientStart.send().registration(TestObjectTwo.class, new TestObserver());
			clientStart.send().object(new Login());
			try {
				Thread.sleep(TimeUnit.SECONDS.toMillis(30));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			clientStart.send().unRegistration(TestObjectTwo.class);
		} catch (CommunicationAlreadySpecifiedException | StartFailedException e) {
			e.printStackTrace();
		}
	}

	private static void register() throws CommunicationAlreadySpecifiedException {
		clientStart.getCommunicationRegistration().register(TestObject.class, (user, o) -> System.out.println("Received " + o.getHello() + " from Server"));
		clientStart.getCommunicationRegistration().register(TestObjectThree.class, (user, o) -> System.out.println("----\n" + o.getMsg() + "\n----"));
	}

	private static void start() throws StartFailedException {
		clientStart.launch();
		clientStart.addFallBackDeSerialization(new TestDeSerializer());
		clientStart.addFallBackSerialization(new TestSerializer());
		clientStart.addDisconnectedHandler(client -> System.out.println("Byebye"));
	}
}

class TestObserver implements Observer {

	@Override
	public void update(Observable o, Object arg) {
		if (arg != null) {
			parse(arg);
		}
	}

	private void parse(Object arg) {
		if (arg.getClass().equals(NewEntryEvent.class)) {
			System.out.println("Received push from Server about: " + ((NewEntryEvent) arg).getObject());
		} else if (arg.getClass().equals(DeletedEntryEvent.class)) {
			System.out.println("Entry deleted for" + ((DeletedEntryEvent) arg).getaClass());
		}
	}
}