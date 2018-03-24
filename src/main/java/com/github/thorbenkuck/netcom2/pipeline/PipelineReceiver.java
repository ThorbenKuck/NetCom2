package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.interfaces.TriPredicate;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This is an inner Class, used for containing the {@link TriPredicate} and the connected {@link TriPredicate}, so that
 * they are clustered.
 *
 * @param <T> The object, that is handled by the {@link OnReceiveTriple} and tested by the {@link TriPredicate}
 */
@APILevel
class PipelineReceiver<T> {

	private final OnReceiveTriple<T> onReceive;
	private final Queue<TriPredicate<Connection, Session, T>> predicates = new LinkedList<>();

	/**
	 * The PipelineReceiver requires the {@link OnReceiveTriple}.
	 * Since the {@link TriPredicate} is optional, it is not required in the constructor
	 *
	 * Null is a valid parameter.
	 *
	 * @param onReceive the OnReceive to be handled
	 */
	@APILevel
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
	@APILevel
	static PipelineReceiver empty() {
		final PipelineReceiver<Object> pipelineReceiver = new PipelineReceiver<>(null);
		pipelineReceiver.addTriPredicate(((object, object2, object3) -> false));
		return pipelineReceiver;
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
		if (! (o instanceof PipelineReceiver)) return false;

		final PipelineReceiver<?> that = (PipelineReceiver<?>) o;

		return onReceive.equals(that.onReceive);
	}

	@APILevel
	final void addTriPredicate(final TriPredicate<Connection, Session, T> triPredicate) {
		NetCom2Utils.parameterNotNull(triPredicate);
		predicates.add(triPredicate);
	}

	@APILevel
	final boolean test(Connection connection, Session session, T t) {
		NetCom2Utils.parameterNotNull(connection, session, t);
		final Queue<TriPredicate<Connection, Session, T>> predicateTemp = new LinkedList<>(predicates);
		while (predicateTemp.peek() != null) {
			if (! predicateTemp.remove().test(connection, session, t)) {
				return false;
			}
		}
		return true;
	}

	@APILevel
	final OnReceiveTriple<T> getOnReceive() {
		return onReceive;
	}
}
