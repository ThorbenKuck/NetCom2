package com.github.thorbenkuck.netcom2.interfaces;

/**
 * A Factory interface is used to access any Object.
 * <p>
 * In contrast to the {@link SimpleFactory}, this interface expects a parameter inside of the {@link Factory#create(Object)} method.
 * This might be used to access the Object. It either is the base or a parameter of the newly created Object
 * <p>
 * In contrast to the {@link Adapter} interface, the call of the {@link #create(Object)} method will <b>ALWAYS</b> access
 * a new Instance of the said type T.
 *
 * @param <F> The Type of the Object, required to access the T
 * @param <T> The Type of the Object, which is instantiated by calling the {@link #create(Object)} method
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface Factory<F, T> {

	/**
	 * By calling this method, this Class instantiates (creates) the new Object.
	 * It should <b>NOT</b> return any previously created instance, but a new instance every time the Method is called.
	 *
	 * @param f the Object, required to access the object, which this factory should access.
	 * @return a new Instance of the defined Type.
	 */
	T create(final F f);

}
