package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.interfaces.TriConsumer;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

@FunctionalInterface
public interface OnReceiveTriple<O> extends TriConsumer<Connection, Session, O>, CanBeRegistered, ReceiveFamily {
}
