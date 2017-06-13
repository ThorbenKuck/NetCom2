package de.thorbenkuck.netcom2.network.shared;

@FunctionalInterface
public interface Expectable {

	void andAwaitReceivingOfClass(Class clazz) throws InterruptedException;

}
