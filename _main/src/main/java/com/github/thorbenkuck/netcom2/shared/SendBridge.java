package com.github.thorbenkuck.netcom2.shared;


import com.github.thorbenkuck.netcom2.shared.clients.Client;

public interface SendBridge {

    static SendBridge openTo(Client client) {
        return new NativeClientSendBridge(client);
    }

    void send(Object object);

}
