package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

import java.util.function.BiConsumer;

/**
 * @param <T> The Object, that should be Consumed beneath the Session
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface OnReceive<T> extends BiConsumer<Session, T>, CanBeRegistered, ReceiveFamily {
}
