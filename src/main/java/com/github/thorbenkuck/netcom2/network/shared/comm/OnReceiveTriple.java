package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.interfaces.TriConsumer;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

/**
 * @param <T> The Object, that should be Consumed besides the {@link Session} and the {@link Connection}
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface OnReceiveTriple<T> extends TriConsumer<Connection, Session, T>, CanBeRegistered, ReceiveFamily {
}
