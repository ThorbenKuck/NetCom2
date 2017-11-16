package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Session;

public interface MultipleConnections {

	Awaiting createNewConnection(final Session client, final Class key);

}
