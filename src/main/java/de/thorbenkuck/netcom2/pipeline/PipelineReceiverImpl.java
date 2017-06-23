package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.interfaces.TriPredicate;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;

import java.util.LinkedList;
import java.util.Queue;

class PipelineReceiverImpl<T> {

	private final OnReceiveTriple<T> onReceive;
	private final Queue<TriPredicate<Connection, Session, T>> predicates = new LinkedList<>();

	PipelineReceiverImpl(OnReceiveTriple<T> onReceive) {
		this.onReceive = onReceive;
	}

	final void addTriPredicate(TriPredicate<Connection, Session, T> triPredicate) {
		predicates.add(triPredicate);
	}

	final boolean test(Connection connection, Session session, T t) {
		Queue<TriPredicate<Connection, Session, T>> predicateTemp = new LinkedList<>(predicates);
		while (predicateTemp.peek() != null) {
			if (! predicateTemp.remove().test(connection, session, t)) {
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

	final OnReceiveTriple<T> getOnReceive() {
		return onReceive;
	}
}
