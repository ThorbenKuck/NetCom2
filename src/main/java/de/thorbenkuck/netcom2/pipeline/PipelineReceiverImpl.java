package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.interfaces.TriPredicate;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

class PipelineReceiverImpl<T> {

	private final OnReceiveTriple<T> onReceive;
	private final Queue<TriPredicate<Connection, Session, T>> predicates = new LinkedList<>();

	PipelineReceiverImpl(OnReceiveTriple<T> onReceive) {
		Objects.requireNonNull(onReceive);
		this.onReceive = onReceive;
	}

	final void addTriPredicate(TriPredicate<Connection, Session, T> triPredicate) {
		Objects.requireNonNull(triPredicate);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (! (o instanceof PipelineReceiverImpl)) return false;

		PipelineReceiverImpl<?> that = (PipelineReceiverImpl<?>) o;

		return onReceive.equals(that.onReceive);
	}

	@Override
	public int hashCode() {
		return onReceive.hashCode();
	}

	final OnReceiveTriple<T> getOnReceive() {
		return onReceive;
	}

	static final PipelineReceiverImpl empty() {
		PipelineReceiverImpl<Object> pipelineReceiver = new PipelineReceiverImpl<>(null);
		pipelineReceiver.addTriPredicate(((object, object2, object3) -> false));
		return pipelineReceiver;
	}
}
