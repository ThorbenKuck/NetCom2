package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.ListenAndExpect;

/**
 * Encapsulates an {@link ListenAndExpect} to be acceptable as an {@link Callback}
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
class CallbackListenerWrapper implements Callback<Object> {

	@APILevel
	private final ListenAndExpect listener;

	@APILevel
	CallbackListenerWrapper(@APILevel final ListenAndExpect listener) {
		this.listener = listener;
	}

	/** {@inheritDoc} */
	@Override
	public void accept(final Object object) {
		listener.tryAccept(object.getClass());
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAcceptable(final Object object) {
		return listener.isAcceptable(object);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isRemovable() {
		return listener.isRemovable();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{ " + listener.toString() + "}";
	}
}
