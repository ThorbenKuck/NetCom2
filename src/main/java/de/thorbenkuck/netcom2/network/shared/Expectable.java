package de.thorbenkuck.netcom2.network.shared;

@FunctionalInterface
public interface Expectable {

	void andWaitFor(Class clazz) throws InterruptedException;

}
