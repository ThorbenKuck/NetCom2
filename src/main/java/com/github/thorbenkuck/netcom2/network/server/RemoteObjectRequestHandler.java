package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.exceptions.RemoteRequestException;
import com.github.thorbenkuck.netcom2.interfaces.RemoteObjectRegistration;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

@APILevel
class RemoteObjectRequestHandler implements OnReceiveTriple<RemoteAccessCommunicationRequest> {

	private final RemoteObjectRegistration remoteObjectRegistration;
	private final Logging logging = Logging.unified();

	@APILevel
	RemoteObjectRequestHandler(final RemoteObjectRegistration remoteObjectRegistration) {
		this.remoteObjectRegistration = remoteObjectRegistration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(final Connection connection, final Session session, final RemoteAccessCommunicationRequest remoteAccessCommunicationRequest) {
		NetCom2Utils.parameterNotNull(connection, remoteAccessCommunicationRequest);
		try {
			connection.write(remoteObjectRegistration.run(remoteAccessCommunicationRequest));
		} catch (RemoteRequestException e) {
			logging.error("Could not run RemoteObjectRequest", e);
		}
	}
}
