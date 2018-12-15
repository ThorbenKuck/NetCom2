package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This interface is created equally to the {@link java.util.function.Predicate}, but excepting 3 types as arguments
 * <p>
 * Its primary use is to enable the {@link com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineCondition}, which
 * utilizes this predicate to evaluate whether or not an {@link com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple}
 * should be executed or not.
 *
 * @param <T> T the first type to test
 * @param <U> U the second type to test
 * @param <V> V the third type tot test
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface TriPredicate<T, U, V> {

	/**
	 * Evaluates the predicate of the given arguments.
	 *
	 * @param t an Object of the first Type
	 * @param u an Object of the second Type
	 * @param v an Object of the third Type
	 * @return true, if the predicate is successful, else false
	 */
	boolean test(final T t, final U u, final V v);

	/**
	 * Returns a composed triPredicate that represents a short-circuiting logical
	 * AND of this predicate and another.  When evaluating the composed
	 * triPredicate, if this triPredicate is {@code false}, then the {@code other}
	 * triPredicate is not evaluated.
	 * <p>Any exceptions thrown during evaluation of either triPredicate are relayed
	 * to the caller; if evaluation of this triPredicate throws an exception, the
	 * {@code other} triPredicate will not be evaluated.
	 *
	 * @param other a triPredicate that will be logically-ANDed with this
	 *              predicate
	 * @return a composed triPredicate that represents the short-circuiting logical
	 * AND of this predicate and the {@code other} triPredicate
	 * @throws NullPointerException if other is null
	 */
	default TriPredicate<T, U, V> and(TriPredicate<? super T, ? super U, ? super V> other) {
		NetCom2Utils.assertNotNull(other);
		return (t, u, v) -> test(t, u, v) && other.test(t, u, v);
	}

	/**
	 * Returns a triPredicate that represents the logical negation of this
	 * triPredicate.
	 *
	 * @return a triPredicate that represents the logical negation of this
	 * triPredicate
	 */
	default TriPredicate<T, U, V> negate() {
		return (t, u, v) -> !test(t, u, v);
	}

	/**
	 * Returns a composed triPredicate that represents a short-circuiting logical
	 * OR of this triPredicate and another.  When evaluating the composed
	 * triPredicate, if this predicate is {@code true}, then the {@code other}
	 * triPredicate is not evaluated.
	 * <p>
	 * Any exceptions thrown during evaluation of either triPredicate are relayed
	 * to the caller; if evaluation of this triPredicate throws an exception, the
	 * {@code other} triPredicate will not be evaluated.
	 *
	 * @param other a triPredicate that will be logically-ORed with this
	 *              triPredicate
	 * @return a composed triPredicate that represents the short-circuiting logical
	 * OR of this triPredicate and the {@code other} predicate
	 * @throws NullPointerException if other is null
	 */
	default TriPredicate<T, U, V> or(TriPredicate<? super T, ? super U, ? super V> other) {
		NetCom2Utils.assertNotNull(other);
		return (t, u, v) -> test(t, u, v) || other.test(t, u, v);
	}
}
