package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.interfaces.TriPredicate;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.utility.Requirements;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This is an inner Class, used for containing the {@link TriPredicate} and the connected {@link TriPredicate}, so that
 * they are clustered.
 *
 * @param <T> The object, that is handled by the {@link OnReceiveTriple} and tested by the {@link TriPredicate}
 */
class PipelineReceiver<T> {

	private final OnReceiveTriple<T> onReceive;
	private final Queue<TriPredicate<Connection, Session, T>> predicates = new LinkedList<>();

	/**
	 * The PipelineReceiver requires the {@link OnReceiveTriple}.
	 * Since the {@link TriPredicate} is optional, it is not required in the constructor
	 *
	 * @param onReceive the OnReceive to be handled
	 */
	PipelineReceiver(final OnReceiveTriple<T> onReceive) {
		this.onReceive = onReceive;
	}

	/**
	 * This call returns an Null-Object-PipelineReceiver.
	 * This means:
	 * the {@link OnReceiveTriple} will be null will be contained
	 * An {@link TriPredicate}, which always returns false will be added
	 *
	 * @return an PipelineReceiver Null-Object
	 */
	static final PipelineReceiver empty() {
		final PipelineReceiver<Object> pipelineReceiver = new PipelineReceiver<>(null);
		pipelineReceiver.addTriPredicate(((object, object2, object3) -> false));
		return pipelineReceiver;
	}

	final void addTriPredicate(final TriPredicate<Connection, Session, T> triPredicate) {
		Requirements.parameterNotNull(triPredicate);
		predicates.add(triPredicate);
	}

	final boolean test(Connection connection, Session session, T t) {
		Requirements.parameterNotNull(connection, session, t);
		final Queue<TriPredicate<Connection, Session, T>> predicateTemp = new LinkedList<>(predicates);
		while (predicateTemp.peek() != null) {
			if (! predicateTemp.remove().test(connection, session, t)) {
				return false;
			}
		}
		return true;
	}

	final OnReceiveTriple<T> getOnReceive() {
		return onReceive;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return onReceive.hashCode();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof PipelineReceiver)) return false;

		final PipelineReceiver<?> that = (PipelineReceiver<?>) o;

		return onReceive.equals(that.onReceive);
	}
}