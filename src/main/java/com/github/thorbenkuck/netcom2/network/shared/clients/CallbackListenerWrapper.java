package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.ListenAndExpect;

@APILevel
class CallbackListenerWrapper implements Callback<Object> {

	@APILevel
	private final ListenAndExpect listener;

	@APILevel
	CallbackListenerWrapper(@APILevel final ListenAndExpect listener) {
		this.listener = listener;
	}

	@Override
	public void accept(final Object object) {
		listener.tryAccept(object.getClass());
	}

	@Override
	public boolean isAcceptable(final Object object) {
		return listener.isAcceptable(object);
	}

	@Override
	public boolean isRemovable() {
		return listener.isRemovable();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{ " + listener.toString() + "}";
	}
}
