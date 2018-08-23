package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.network.shared.ReceiveFamily;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

import java.util.function.BiConsumer;

public interface OnReceive<T> extends BiConsumer<Session, T>, CanBeRegistered, ReceiveFamily {
}
