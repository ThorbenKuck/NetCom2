package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.annotations.Synchronized;
import de.thorbenkuck.netcom2.network.shared.CallBack;
import de.thorbenkuck.netcom2.network.shared.Feasible;

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
