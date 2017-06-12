package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.interfaces.TriPredicate;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

class PipelineReceiverImpl<T> {

	private final OnReceiveTriple<T> onReceive;
	private final Queue<Predicate<Session>> predicates = new LinkedList<>();
	private final Queue<BiPredicate<Session, T>> biPredicates = new LinkedList<>();
	private final Queue<TriPredicate<Connection, Session, T>> triPredicates = new LinkedList<>();

	PipelineReceiverImpl(OnReceiveTriple<T> onReceive) {
		this.onReceive = onReceive;
	}

	final void addPredicate(Predicate<Session> userPredicate) {
		predicates.add(userPredicate);
	}

	final void addBiPredicate(BiPredicate<Session, T> biPredicate) {
		biPredicates.add(biPredicate);
	}

	final void addTriPredicate(TriPredicate<Connection, Session, T> triPredicate) {
		triPredicates.add(triPredicate);
	}

	final boolean test(Connection connection, Session session, T t) {
		Queue<Predicate<Session>> predicateTemp = new LinkedList<>(predicates);
		while (predicateTemp.peek() != null) {
			if (! predicateTemp.remove().test(session)) {
				return false;
			}
		}

		Queue<BiPredicate<Session, T>> biPredicateTemp = new LinkedList<>(biPredicates);
		while (biPredicateTemp.peek() != null) {
			if (! biPredicateTemp.remove().test(session, t)) {
				return false;
			}
		}

		Queue<TriPredicate<Connection, Session, T>> triPredicatesTemp = new LinkedList<>(triPredicates);
		while (triPredicatesTemp.peek() != null) {
			if (! triPredicatesTemp.remove().test(connection, session, t)) {
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
