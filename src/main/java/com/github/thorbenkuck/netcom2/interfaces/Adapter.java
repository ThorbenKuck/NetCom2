package com.github.thorbenkuck.netcom2.interfaces;

/**
 * This Adapter interface takes something and turns it into anther type, state in the generic types.
 *
 * The Difference to the {@link Factory} is, that the original type is not necessary used within the new Object,
 * whilst the {@link Factory} needs the Object to create the new Object. Also, the instantiation of the Adapter interface
 * might not create a new Instance, every time the {@link Adapter#get(Object)} is called
 *
 * The Adapter interface may be used, to turn adapt to something. If you, for example use an {@link String} within your
 * Application and get an int injected, you might provide an interface to create an {@link Adapter} to convert the
 * int into an {@link String}. This Adapter might look like this:
 *
 * <code>
 *     int param = ...
 *     Adapter<Integer, String> adapter = integer -> Integer.toString(integer);
 *     String result = adapter.get(param);
 * </code>
 *
 * If you use an Adapter within your Application, you may easily change the behaviour, using the strategy pattern. You
 * can make your behaviour easily interchangeable by providing a setter for the said Adapter
 *
 * @param <F> the type, which originally is present (from)
 * @param <T> the type, which the originally present Type should be turned into (to)
 */
@FunctionalInterface
public interface Adapter<F, T> {

	/**
	 * This method is used, to adapt any Object (F) to another Object (T)
	 *
	 * By calling this Method, the responsible Object T is returned.
	 * Whether or not the Object T is created or reused is not determined, in contrast to the {@link Factory} interface
	 *
	 * @param f the Object, that should be adapted
	 * @return the adaptation of the parameter
	 */
	T get(final F f);
}
