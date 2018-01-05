package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelResponse;

class RemoteAccessResponseHandler implements OnReceive<RemoteAccessCommunicationModelResponse> {

	private final RemoteAccessBlockRegistration remoteAccessBlockRegistration;

	RemoteAccessResponseHandler(final RemoteAccessBlockRegistration remoteAccessBlockRegistration) {
		this.remoteAccessBlockRegistration = remoteAccessBlockRegistration;
	}

	/**
	 * Performs this operation on the given arguments.
	 *
	 * @param session  the first input argument
	 * @param response the second input argument
	 */
	@Override
	public void accept(final Session session, final RemoteAccessCommunicationModelResponse response) {
		remoteAccessBlockRegistration.release(response);
	}
}
