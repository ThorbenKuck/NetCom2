package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.shared.CallBack;
import com.github.thorbenkuck.netcom2.network.shared.Feasible;

@Synchronized
class CallBackFeasibleWrapper implements CallBack<Object> {

	private final Feasible<Class> feasible;

	CallBackFeasibleWrapper(Feasible<Class> feasible) {
		this.feasible = feasible;
	}

	@Override
	public void accept(Object object) {
		feasible.tryAccept(object.getClass());
	}

	@Override
	public boolean isAcceptable(Object object) {
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
