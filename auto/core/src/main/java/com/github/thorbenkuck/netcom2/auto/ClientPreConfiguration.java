package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.network.client.ClientStart;

import java.util.function.BiConsumer;

public interface ClientPreConfiguration extends BiConsumer<ClientStart, ObjectRepository> {
}
