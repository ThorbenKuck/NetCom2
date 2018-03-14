package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.shared.Callback;
import com.github.thorbenkuck.netcom2.network.shared.Feasible;

@APILevel
@Synchronized
class CallbackFeasibleWrapper implements Callback<Object> {

	private final Feasible<Class> feasible;

	@APILevel
	CallbackFeasibleWrapper(final Feasible<Class> feasible) {
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
