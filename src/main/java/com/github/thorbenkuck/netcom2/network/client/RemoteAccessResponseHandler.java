package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationResponse;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This Class handles the {@link RemoteAccessCommunicationResponse} received over the network.
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.client.RemoteAccessResponseHandlerTest")
class RemoteAccessResponseHandler implements OnReceiveSingle<RemoteAccessCommunicationResponse> {

	private final RemoteAccessBlockRegistration remoteAccessBlockRegistration;

	@APILevel
	RemoteAccessResponseHandler(final RemoteAccessBlockRegistration remoteAccessBlockRegistration) {
		this.remoteAccessBlockRegistration = remoteAccessBlockRegistration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(final RemoteAccessCommunicationResponse response) {
		NetCom2Utils.parameterNotNull(response);
		remoteAccessBlockRegistration.release(response);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "RemoteAccessResponseHandler{" +
				"remoteAccessBlockRegistration=" + remoteAccessBlockRegistration +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RemoteAccessResponseHandler)) return false;

		RemoteAccessResponseHandler that = (RemoteAccessResponseHandler) o;

		return remoteAccessBlockRegistration.equals(that.remoteAccessBlockRegistration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return remoteAccessBlockRegistration.hashCode();
	}
}
