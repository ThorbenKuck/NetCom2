package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationResponse;

@APILevel
class RemoteAccessResponseHandler implements OnReceive<RemoteAccessCommunicationResponse> {

	private final RemoteAccessBlockRegistration remoteAccessBlockRegistration;

	@APILevel
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
	public void accept(final Session session, final RemoteAccessCommunicationResponse response) {
		remoteAccessBlockRegistration.release(response);
	}
}
