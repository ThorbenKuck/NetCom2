package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.keller.datatypes.interfaces.TriConsumer;
import com.github.thorbenkuck.netcom2.network.shared.ReceiveFamily;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

public interface OnReceiveTriple<T> extends TriConsumer<ConnectionContext, Session, T>, CanBeRegistered, ReceiveFamily {

	default void execute(ConnectionContext connectionContext, Session session, T t) {
		beforeExecution();
		try {
			accept(connectionContext, session, t);
			successfullyExecuted();
		} catch (Exception e) {
			exceptionEncountered(e);
			throw e;
		}
	}

}
