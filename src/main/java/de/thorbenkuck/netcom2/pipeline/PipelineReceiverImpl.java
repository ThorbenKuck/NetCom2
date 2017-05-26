package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Predicate;

class PipelineReceiverImpl<T> {

	private final OnReceive<T> onReceive;
	private final Queue<Predicate<Session>> predicates = new LinkedList<>();

	PipelineReceiverImpl(OnReceive<T> onReceive) {
		this.onReceive = onReceive;
	}

	final void addPredicate(Predicate<Session> userPredicate) {
		predicates.add(userPredicate);
	}

	final boolean test(Session session) {
		while (predicates.peek() != null) {
			if (! predicates.remove().test(session)) {
				return false;
			}
		}
		return true;
	}

	public boolean equals(Object o) {
		if (o == null || ! PipelineReceiverImpl.class.equals(o.getClass())) {
			return false;
		}

		if (this == o) {
			return true;
		}

		return onReceive.equals(((PipelineReceiverImpl) o).getOnReceive());
	}

	final OnReceive<T> getOnReceive() {
		return onReceive;
	}
}
