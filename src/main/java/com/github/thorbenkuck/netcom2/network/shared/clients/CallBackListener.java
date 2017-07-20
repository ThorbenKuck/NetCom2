package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.network.shared.CallBack;
import com.github.thorbenkuck.netcom2.network.shared.ListenAndExpect;

public class CallBackListener implements CallBack<Object> {

	private ListenAndExpect listener;

	public CallBackListener(ListenAndExpect listener) {
		this.listener = listener;
	}

	@Override
	public void accept(Object object) {
		listener.tryAccept(object.getClass());
	}

	@Override
	public boolean isAcceptable(Object object) {
		return listener.isAcceptable(object.getClass());
	}

	@Override
	public boolean isRemovable() {
		return listener.isRemovable();
	}
}
