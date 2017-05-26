package test;

import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.network.interfaces.ClientStart;
import de.thorbenkuck.netcom2.network.shared.cache.AbstractCacheObserver;
import de.thorbenkuck.netcom2.network.shared.cache.CacheObservable;

public class ClientStartTest {

	private static ClientStart clientStart;
	private static int port = 44444;

	public static void main(String[] args) {
		clientStart = ClientStart.of("localhost", port);
//		clientStart.setSocketFactory((port, address) -> {
//			try {
//				return SSLSocketFactory.getDefault().createSocket(address, port);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			try {
//				return new Socket(address, port);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			return null;
//		});

		try {
			register();
			start();
			clientStart.send().objectToServer(new Login());
			clientStart.send().objectToServer(new TestObject("Hello"));
			clientStart.send().registrationToServer(TestObjectTwo.class, new TestObserver());
		} catch (StartFailedException e) {
			e.printStackTrace();
		}
	}

	private static void register() {
		clientStart.getCommunicationRegistration().register(TestObject.class).addLast((user, o) -> System.out.println("Received " + o.getHello() + " from Server"));
		clientStart.getCommunicationRegistration().register(TestObjectThree.class).addLast((user, o) -> System.out.println("----\n" + o.getMsg() + "\n----"));
	}

	private static void start() throws StartFailedException {
		clientStart.launch();
		clientStart.addFallBackDeSerialization(new TestDeSerializer());
		clientStart.addFallBackSerialization(new TestSerializer());
		clientStart.addDisconnectedHandler(client -> System.out.println("Bye bye lieber Server"));
	}
}

class TestObserver extends AbstractCacheObserver<TestObjectTwo> {
	TestObserver() {
		super(TestObjectTwo.class);
	}

	@Override
	public void newEntry(TestObjectTwo newEntry, CacheObservable observable) {
		System.out.println("[NEW ENTRY] Received push from Server about: " + newEntry);
	}

	@Override
	public void updatedEntry(TestObjectTwo updatedEntry, CacheObservable observable) {
		System.out.println("[UPDATE] Received push from Server about: " + updatedEntry);
	}

	@Override
	public void deletedEntry(TestObjectTwo deletedEntry, CacheObservable observable) {
		System.out.println("[DELETED] Received push from Server about: " + deletedEntry);
	}
}