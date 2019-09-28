package com.github.thorbenkuck.netcom2.shared;

import com.github.thorbenkuck.netcom2.interfaces.ReceiveFamily;
import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

import java.util.function.BiConsumer;

public interface OnReceive<T> extends BiConsumer<Session, T>, CanBeRegistered, ReceiveFamily {
}
