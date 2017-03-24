package de.thorbenkuck.netcom2.network.shared.cache;

public class UpdatedEntryEvent {

	private Object object;

	public UpdatedEntryEvent(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return object;
	}

	@Override
	public String toString() {
		return "UpdatedEntryEvent{" +
				"object=" + object +
				'}';
	}
}
