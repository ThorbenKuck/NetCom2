package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.pipeline.CanBeRegistered;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface OnReceive<O> extends BiConsumer<Session, O>, CanBeRegistered {

	void accept(Session session, O o);

}
