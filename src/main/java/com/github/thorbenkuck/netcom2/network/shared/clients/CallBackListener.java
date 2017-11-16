package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.network.shared.CallBack;
import com.github.thorbenkuck.netcom2.network.shared.ListenAndExpect;

public class CallBackListener implements CallBack<Object> {

	private final ListenAndExpect listener;

	public CallBackListener(final ListenAndExpect listener) {
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
