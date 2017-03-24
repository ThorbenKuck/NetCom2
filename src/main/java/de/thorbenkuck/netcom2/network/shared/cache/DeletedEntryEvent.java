package de.thorbenkuck.netcom2.network.shared.cache;

public class DeletedEntryEvent {

	private Class aClass;

	public DeletedEntryEvent(Class aClass) {
		this.aClass = aClass;
	}

	public Class getCorrespondingClass() {
		return aClass;
	}

	@Override
	public String toString() {
		return "DeletedEntryEvent{" +
				"aClass=" + aClass +
				'}';
	}
}
