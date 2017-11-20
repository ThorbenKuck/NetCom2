package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.ListenAndExpect;

public class CallbackListener implements Callback<Object> {

	private final ListenAndExpect listener;

	public CallbackListener(final ListenAndExpect listener) {
		this.listener = listener;
	}

	@Override
	public void accept(final Object object) {
		listener.tryAccept(object.getClass());
	}

	@Override
	public boolean isAcceptable(final Object object) {
		return listener.isAcceptable(object.getClass());
	}

	@Override
	public boolean isRemovable() {
		return listener.isRemovable();
	}
}
