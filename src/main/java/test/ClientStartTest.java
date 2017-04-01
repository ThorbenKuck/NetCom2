package test;

import de.thorbenkuck.netcom2.exceptions.CommunicationAlreadySpecifiedException;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.network.interfaces.ClientStart;
import de.thorbenkuck.netcom2.network.shared.cache.AbstractCacheObserver;
import de.thorbenkuck.netcom2.network.shared.cache.DeletedEntryEvent;
import de.thorbenkuck.netcom2.network.shared.cache.NewEntryEvent;
import de.thorbenkuck.netcom2.network.shared.cache.UpdatedEntryEvent;

import java.util.Observable;
import java.util.concurrent.TimeUnit;

public class ClientStartTest {

	private static ClientStart clientStart;
	private static int port = 44444;

	public static void main(String[] args) {
		clientStart = ClientStart.of("localhost", port);

		try {
			register();
			start();
			clientStart.send().objectToServer(new Login());
			clientStart.send().objectToServer(new TestObjectTwo("None"));
			try {
				Thread.sleep(TimeUnit.SECONDS.toMillis(30));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			clientStart.send().unRegistrationToServer(TestObjectTwo.class);
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
		clientStart.addDisconnectedHandler(client -> System.out.println("Bye bye lieber Server"));
	}
}

class TestObserver extends AbstractCacheObserver {
	@Override
	public void newEntry(NewEntryEvent newEntryEvent, Observable observable) {
		System.out.println("[NEW ENTRY] Received push from Server about: " + newEntryEvent.getObject());
	}

	@Override
	public void updatedEntry(UpdatedEntryEvent updatedEntryEvent, Observable observable) {
		System.out.println("[UPDATE] Received push from Server about: " + updatedEntryEvent.getObject());
	}

	@Override
	public void deletedEntry(DeletedEntryEvent deletedEntryEvent, Observable observable) {
		System.out.println("[DELETED] Received push from Server about: " + deletedEntryEvent.getCorrespondingClass());
	}
}