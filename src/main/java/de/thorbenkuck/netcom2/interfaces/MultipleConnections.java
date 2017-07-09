package de.thorbenkuck.netcom2.interfaces;

import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.Session;

public interface MultipleConnections {

	Awaiting createNewConnection(Session client, Class key);

}
