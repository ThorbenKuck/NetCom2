package com.github.thorbenkuck.netcom2.shared.connections;

import java.util.List;

public interface ConnectionHandler {

	void prepare(final byte[] read);

	List<String> takeContents();

}
