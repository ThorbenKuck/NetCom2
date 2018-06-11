package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

import java.util.function.BiConsumer;

public interface OnReceive<T> extends BiConsumer<Session, T>, CanBeRegistered, ReceiveFamily {
}
