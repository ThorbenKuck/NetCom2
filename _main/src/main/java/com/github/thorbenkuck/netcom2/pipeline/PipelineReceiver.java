package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.keller.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.interfaces.TriPredicate;
import com.github.thorbenkuck.netcom2.shared.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.shared.Session;
import com.github.thorbenkuck.netcom2.utils.NetCom2Utils;
import com.github.thorbenkuck.network.connection.ConnectionContext;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This is an inner Class, used for containing the {@link TriPredicate} and the connected {@link TriPredicate}, so that
 * they are clustered.
 *
 * @param <T> The object, that is handled by the {@link OnReceiveTriple} and tested by the {@link TriPredicate}
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
class PipelineReceiver<T> {

	private final OnReceiveTriple<T> onReceive;
	private final Queue<TriPredicate<ConnectionContext, Session, T>> predicates = new LinkedList<>();

	/**
	 * The PipelineReceiver requires the {@link OnReceiveTriple}.
	 * Since the {@link TriPredicate} is optional, it is not required in the constructor
	 * <p>
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
	static com.github.thorbenkuck.netcom2.pipeline.PipelineReceiver empty() {
		final com.github.thorbenkuck.netcom2.pipeline.PipelineReceiver<Object> pipelineReceiver = new com.github.thorbenkuck.netcom2.pipeline.PipelineReceiver<>(null);
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
		if (!(o instanceof com.github.thorbenkuck.netcom2.pipeline.PipelineReceiver)) return false;

		final com.github.thorbenkuck.netcom2.pipeline.PipelineReceiver<?> that = (com.github.thorbenkuck.netcom2.pipeline.PipelineReceiver<?>) o;

		return onReceive.equals(that.onReceive);
	}

	/**
	 * Adds a TriPredicate to the internal Queue.
	 * <p>
	 * This method is only meant for internal use.
	 *
	 * @param triPredicate The TriPredicate to add
	 */
	@APILevel
	final void addTriPredicate(final TriPredicate<ConnectionContext, Session, T> triPredicate) {
		NetCom2Utils.parameterNotNull(triPredicate);
		predicates.add(triPredicate);
	}

	/**
	 * Test all TriPredicates and return false early if one returns false.
	 * <p>
	 * This method is only meant for internal use.
	 *
	 * @param connectionContext The connection to test with
	 * @param session           The session to test with
	 * @param t                 The T to test with
	 * @return false if one predicate returns false, true otherwise
	 */
	@APILevel
	final boolean test(ConnectionContext connectionContext, Session session, T t) {
		NetCom2Utils.parameterNotNull(connectionContext, session, t);
		final Queue<TriPredicate<ConnectionContext, Session, T>> predicateTemp = new LinkedList<>(predicates);
		while (predicateTemp.peek() != null) {
			if (!predicateTemp.remove().test(connectionContext, session, t)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get the internal OnReceiveTriple instance.
	 * <p>
	 * This method is only meant for internal use.
	 *
	 * @return The OnReceiveTriple
	 */
	@APILevel
	final OnReceiveTriple<T> getOnReceive() {
		return onReceive;
	}
}
