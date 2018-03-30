package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.Feasible;

/**
 * This Wrapper wraps {@link Feasible} to make it acceptable as a Callback
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
class CallbackFeasibleWrapper implements Callback<Object> {

	private final Feasible<Class> feasible;

	@APILevel
	CallbackFeasibleWrapper(final Feasible<Class> feasible) {
		this.feasible = feasible;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(final Object object) {
		feasible.tryAccept(object.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAcceptable(final Object object) {
		return feasible.isAcceptable(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRemovable() {
		return feasible.isRemovable();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return feasible.toString();
	}
}
