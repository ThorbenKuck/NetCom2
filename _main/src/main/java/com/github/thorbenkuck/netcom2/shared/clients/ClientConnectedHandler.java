package com.github.thorbenkuck.netcom2.shared.clients;

import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

import java.util.function.Consumer;

public interface ClientConnectedHandler extends Consumer<Client> {
}
