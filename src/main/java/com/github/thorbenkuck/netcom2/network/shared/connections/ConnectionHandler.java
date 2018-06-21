package com.github.thorbenkuck.netcom2.network.shared.connections;

import java.util.List;

public interface ConnectionHandler {

	static ConnectionHandler create() {
		return new NativeConnectionHandler();
	}

	void prepare(byte[] read);

	List<String> takeContents();

}
