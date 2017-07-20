package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.pipeline.CanBeRegistered;

import java.util.function.Consumer;

public interface OnReceiveSingle<O> extends Consumer<O>, CanBeRegistered {

}
