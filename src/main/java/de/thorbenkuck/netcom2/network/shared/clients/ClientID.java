package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.annotations.Synchronized;

import java.io.Serializable;
import java.util.UUID;

@Synchronized
public class ClientID implements Serializable {

	private final UUID id;

	private ClientID(UUID id) {
		this.id = id;
	}

	public static ClientID empty() {
		return new ClientID(null);
	}

	public static ClientID create() {
		return new ClientID(UUID.randomUUID());
	}

	public static ClientID fromString(String s) {
		return new ClientID(UUID.fromString(s));
	}

	public static boolean isEmpty(ClientID clientID) {
		return clientID.id == null;
	}

	public void ifEmpty(Runnable runnable) {
		if (isEmpty()) {
			runnable.run();
		}
	}

	public boolean isEmpty() {
		return id == null;
	}

	public void ifNotEmpty(Runnable runnable) {
		if (! isEmpty()) {
			runnable.run();
		}
	}

	@Override
	public boolean equals(Object o) {
		return o != null && (o.getClass().equals(ClientID.class) && ((ClientID) o).id.equals(id));
	}

	@Override
	public String toString() {
		return "{" + (id == null ? "EmptyClientID" : id.toString()) + "}";
	}
}
