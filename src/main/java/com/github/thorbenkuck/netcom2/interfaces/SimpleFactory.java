package com.github.thorbenkuck.netcom2.interfaces;

/**
 * Similarly to the {@link Factory}, this Class creates a new object, declared using the generic type upon calling the {@link #create()}
 * method.
 *
 * @param <T> the return type of the SimpleFactory
 * @version 1.0
 * @see Factory
 * @since 1.0
 */
@FunctionalInterface
public interface SimpleFactory<T> {

	/**
	 * By calling this method, this Class instantiates (creates) the new Object.
	 * It should <b>NOT</b> return any previously created instance, but a new instance every time the Method is called.
	 *
	 * @return a new Instance of the defined Type.
	 */
	T create();

}
