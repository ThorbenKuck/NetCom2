package com.github.thorbenkuck.netcom2.network.shared;

@FunctionalInterface
public interface Expectable {

	void andWaitFor(final Class clazz) throws InterruptedException;

}
