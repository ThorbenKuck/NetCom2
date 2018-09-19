package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.annotations.Experimental;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.shared.connections.NativeUDPConnectorCore;

import java.net.SocketAddress;

public interface ConnectorCore {

	static ConnectorCore nio(ClientFactory clientFactory) {
		return new NativeNIOConnectorCore(clientFactory);
	}

	static ConnectorCore tcp(ClientFactory clientFactory) {
		return new NativeTCPConnectorCore(clientFactory);
	}

	@Experimental
	static ConnectorCore udp(ClientFactory clientFactory) {
		return new NativeUDPConnectorCore(clientFactory);
	}

	void clear();

	void establishConnection(SocketAddress socketAddress) throws StartFailedException;

	void handleNext() throws ClientConnectionFailedException;

	void disconnect();
}
