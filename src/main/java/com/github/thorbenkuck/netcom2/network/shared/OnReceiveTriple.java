package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.keller.datatypes.interfaces.TriConsumer;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

public interface OnReceiveTriple<T> extends TriConsumer<Connection, Session, T>, CanBeRegistered, ReceiveFamily {
}
