package de.thorbenkuck.netcom2.interfaces;

import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;

public interface Pipeline<T> {
	void addLast(OnReceive<T> pipelineService);

	void addFirst(OnReceive<T> pipelineService);

	void remove(OnReceive<T> pipelineService);

	void clear();

	void run(User user, Object e);

	void close();

	void open();
}
