package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

import java.util.function.Consumer;

/**
 * @param <T> The Object, that should be Consumed
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface OnReceiveSingle<T> extends Consumer<T>, CanBeRegistered, ReceiveFamily {
}
