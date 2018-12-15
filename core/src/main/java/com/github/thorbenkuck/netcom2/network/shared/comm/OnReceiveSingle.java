package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.network.shared.ReceiveFamily;
import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

import java.util.function.Consumer;

@FunctionalInterface
public interface OnReceiveSingle<T> extends Consumer<T>, CanBeRegistered, ReceiveFamily {
}
