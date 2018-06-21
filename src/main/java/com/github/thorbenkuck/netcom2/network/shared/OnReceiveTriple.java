package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.keller.datatypes.interfaces.TriConsumer;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

public interface OnReceiveTriple<T> extends TriConsumer<ConnectionContext, Session, T>, CanBeRegistered, ReceiveFamily {
}
