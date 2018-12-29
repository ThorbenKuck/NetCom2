package com.github.thorbenkuck.netcom2.auto;

import com.github.thorbenkuck.netcom2.exceptions.NetworkInterfaceFactoryException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;

final class NativeClientFactoryFinalizer implements ClientFactoryFinalizer {

	private final ClientStart clientStart;

	NativeClientFactoryFinalizer(ClientStart clientStart) {
		this.clientStart = clientStart;
	}

	@Override
	public ClientStart get() {
		try {
			clientStart.launch();
		} catch (StartFailedException e) {
			throw new NetworkInterfaceFactoryException(e);
		}
		return clientStart;
	}
}
