package com.github.thorbenkuck.netcom2.interfaces;

/**
 * This Adapter interface takes something and turns it into anther type, as stated in the generic types.
 * <p>
 * The difference to the {@link Factory} is, that the original type is not necessarily used within the new Object,
 * whilst the {@link Factory} needs the Object to access the new Object. Also, the instantiation of the Adapter interface
 * might not access a new Instance, every time the {@link Adapter#get(Object)} is called
 * <p>
 * The Adapter interface may be used to adapt to something. If you, for example use a {@link String} within your
 * Application and get an int injected, you might provide an interface to access an {@link Adapter} to convert the
 * int into a {@link String}. This Adapter might look like this:
 * <p>
 * <pre><code>
 * int param = ...
 * Adapter(Integer, String) adapter = integer - Integer.toString(integer);
 * String result = adapter.get(param);
 * </code></pre>
 * <p>
 * If you use an Adapter within your Application, you may easily change the behaviour, using the strategy pattern. You
 * can make your behaviour easily interchangeable by providing a setter for said Adapter
 *
 * @param <F> the type, which originally is present (from)
 * @param <T> the type, which the originally present Type should be turned into (to)
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.shared.EncryptionAdapter
 * @see com.github.thorbenkuck.netcom2.network.shared.DecryptionAdapter
 * @since 1.0
 */
@FunctionalInterface
public interface Adapter<F, T> {

	/**
	 * This method is used to adapt any Object (F) to another Object (T)
	 * <p>
	 * By calling this Method, the responsible Object T is returned.
	 * Whether or not the Object T is created or reused is not determined, in contrast to the {@link Factory} interface
	 *
	 * @param f the Object, that should be adapted
	 * @return the adaptation of the parameter
	 */
	T get(final F f);
}
