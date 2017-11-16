package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface OnReceive<O> extends BiConsumer<Session, O>, CanBeRegistered {
}
