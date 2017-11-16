package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.interfaces.TriPredicate;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

class PipelineReceiverImpl<T> {

	private final OnReceiveTriple<T> onReceive;
	private final Queue<TriPredicate<Connection, Session, T>> predicates = new LinkedList<>();

	PipelineReceiverImpl(final OnReceiveTriple<T> onReceive) {
		Objects.requireNonNull(onReceive);
		this.onReceive = onReceive;
	}

	static final PipelineReceiverImpl empty() {
		final PipelineReceiverImpl<Object> pipelineReceiver = new PipelineReceiverImpl<>(null);
		pipelineReceiver.addTriPredicate(((object, object2, object3) -> false));
		return pipelineReceiver;
	}

	final void addTriPredicate(final TriPredicate<Connection, Session, T> triPredicate) {
		Objects.requireNonNull(triPredicate);
		predicates.add(triPredicate);
	}

	final boolean test(final Connection connection, final Session session, final T t) {
		final Queue<TriPredicate<Connection, Session, T>> predicateTemp = new LinkedList<>(predicates);
		while (predicateTemp.peek() != null) {
			if (!predicateTemp.remove().test(connection, session, t)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return onReceive.hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof PipelineReceiverImpl)) return false;

		final PipelineReceiverImpl<?> that = (PipelineReceiverImpl<?>) o;

		return onReceive.equals(that.onReceive);
	}

	final OnReceiveTriple<T> getOnReceive() {
		return onReceive;
	}
}
