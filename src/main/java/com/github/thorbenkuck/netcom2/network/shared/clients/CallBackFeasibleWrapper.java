package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.shared.CallBack;
import com.github.thorbenkuck.netcom2.network.shared.Feasible;

@Synchronized
class CallBackFeasibleWrapper implements CallBack<Object> {

	private final Feasible<Class> feasible;

	CallBackFeasibleWrapper(final Feasible<Class> feasible) {
		this.feasible = feasible;
	}

	@Override
	public void accept(final Object object) {
		feasible.tryAccept(object.getClass());
	}

	@Override
	public boolean isAcceptable(final Object object) {
		return feasible.isAcceptable(object);
	}

	@Override
	public boolean isRemovable() {
		return feasible.isRemovable();
	}

	public String toString() {
		return feasible.toString();
	}
}
