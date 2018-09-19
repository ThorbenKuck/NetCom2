package com.github.thorbenkuck.netcom2.integration;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;

public class TestHandler implements OnReceiveTriple<TestObject> {
	@Override
	public void accept(ConnectionContext connectionContext, Session session, TestObject testObject) {

	}
}
