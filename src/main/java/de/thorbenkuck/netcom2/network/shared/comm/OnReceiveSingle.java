package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.network.shared.Session;

public interface OnReceiveSingle<O> extends OnReceive<O> {

	default void accept(Session session, O o) {
		accept(o);
	}

	void accept(O o);

}
