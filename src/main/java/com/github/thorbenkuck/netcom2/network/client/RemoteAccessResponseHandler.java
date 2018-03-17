package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationResponse;

@APILevel
class RemoteAccessResponseHandler implements OnReceiveSingle<RemoteAccessCommunicationResponse> {

	private final RemoteAccessBlockRegistration remoteAccessBlockRegistration;

	@APILevel
	RemoteAccessResponseHandler(final RemoteAccessBlockRegistration remoteAccessBlockRegistration) {
		this.remoteAccessBlockRegistration = remoteAccessBlockRegistration;
	}

	/**
	 * Performs this operation on the given arguments.
	 *
	 * @param response the second input argument
	 */
	@Override
	public void accept(final RemoteAccessCommunicationResponse response) {
		remoteAccessBlockRegistration.release(response);
	}
}
