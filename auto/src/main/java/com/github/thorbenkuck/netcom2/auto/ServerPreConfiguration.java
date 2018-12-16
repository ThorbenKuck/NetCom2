package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.network.server.ServerStart;

import java.util.function.BiConsumer;

public interface ServerPreConfiguration extends BiConsumer<ServerStart, ObjectRepository> {
}
