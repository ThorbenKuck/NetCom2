package de.thorbenkuck.netcom2.network.shared.cache;

public class DeletedEntryEvent {

	private Class aClass;

	public DeletedEntryEvent(Class aClass) {
		this.aClass = aClass;
	}

	public Class getaClass() {
		return aClass;
	}
}
