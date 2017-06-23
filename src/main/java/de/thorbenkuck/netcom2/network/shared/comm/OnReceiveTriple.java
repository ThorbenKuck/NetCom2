package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.interfaces.TriConsumer;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.pipeline.CanBeRegistered;

@FunctionalInterface
public interface OnReceiveTriple<O> extends TriConsumer<Connection, Session, O>, CanBeRegistered {
}
