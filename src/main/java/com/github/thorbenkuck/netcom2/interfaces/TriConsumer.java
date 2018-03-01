package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This interface is created equally to the {@link java.util.function.Consumer}, but excepting 3 types as arguments
 * <p>
 * Its primary use is to enable the {@link com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple}, which
 * is inherited by this Class
 *
 * @param <T> T the first input type
 * @param <U> U the second input type
 * @param <V> V the third input type
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {

	/**
	 * Accepts 3 arguments and consumes them.
	 *
	 * @param t the first defined Type
	 * @param u the second defined Type
	 * @param v the third defined Type
	 */
	void accept(final T t, final U u, final V v);

	/**
	 * Returns a composed {@code TriConsumer} that performs, in sequence, this
	 * operation followed by the {@code after} operation. If performing either
	 * operation throws an exception, it is relayed to the caller of the
	 * composed operation.  If performing this operation throws an exception,
	 * the {@code after} operation will not be performed.
	 *
	 * @param after the operation to perform after this operation
	 * @return a composed {@code Consumer} that performs in sequence this
	 * operation followed by the {@code after} operation
	 * @throws NullPointerException if {@code after} is null
	 */
	default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
		NetCom2Utils.assertNotNull(after);
		return (T t, U u, V v) -> {
			accept(t, u, v);
			after.accept(t, u, v);
		};
	}
}
