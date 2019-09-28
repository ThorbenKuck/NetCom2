package com.github.thorbenkuck.netcom2.shared;

import com.github.thorbenkuck.keller.datatypes.interfaces.TriConsumer;
import com.github.thorbenkuck.netcom2.interfaces.ReceiveFamily;
import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;
import com.github.thorbenkuck.network.connection.ConnectionContext;

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
