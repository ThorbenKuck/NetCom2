package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.interfaces.RemoteObjectRegistration;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelRequest;

public class RemoteObjectRequestHandler implements OnReceiveTriple<RemoteAccessCommunicationModelRequest> {

	private final RemoteObjectRegistration remoteObjectRegistration;

	public RemoteObjectRequestHandler(final RemoteObjectRegistration remoteObjectRegistration) {
		this.remoteObjectRegistration = remoteObjectRegistration;
	}

	/**
	 * Performs this operation on the given arguments.
	 *
	 * @param session                               the first input argument
	 * @param remoteAccessCommunicationModelRequest the second input argument
	 */
	@Override
	public void accept(final Connection connection, final Session session, final RemoteAccessCommunicationModelRequest remoteAccessCommunicationModelRequest) {
		remoteObjectRegistration.run(remoteAccessCommunicationModelRequest, connection);
	}
}
