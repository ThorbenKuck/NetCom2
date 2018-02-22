package com.github.thorbenkuck.netcom2.interfaces;

/**
 * A Factory interface is used, to create any Object.
 * <p>
 * Differently to the {@link SimpleFactory}, this interface expects a parameter inside of the {@link Factory#create(Object)} method.
 * This might be used to create the Object. It either is the base or an parameter of the newly created Object
 * <p>
 * Differently to the {@link Adapter} interface, the call of the {@link #create(Object)} method will <b>ALWAYS</b> create
 * a new Instance of the said type T.
 *
 * @param <F> The Type of the Object, required to create the T
 * @param <T> The Type of the Object, which is instantiated by calling the {@link #create(Object)} method
 */
@FunctionalInterface
public interface Factory<F, T> {

	/**
	 * By calling this method, this Class instantiates (creates) the new Object.
	 * It should <b>NOT</b> return any previously created instance, but a new instance every time the Method is called.
	 *
	 * @param f the Object, required to create the object, which this factory should create.
	 * @return a new Instance of the defined Type.
	 */
	T create(final F f);

}
