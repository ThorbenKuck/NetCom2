package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

import java.util.function.Consumer;

@FunctionalInterface
public interface OnReceiveSingle<T> extends Consumer<T>, CanBeRegistered, ReceiveFamily {
}
