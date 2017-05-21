package de.thorbenkuck.netcom2.interfaces;

import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.pipeline.PipelineCondition;

public interface Pipeline<T> {
	PipelineCondition<T> addLast(OnReceive<T> pipelineService);

	PipelineCondition<T> addFirst(OnReceive<T> pipelineService);

	void remove(OnReceive<T> pipelineService);

	void clear();

	void run(User user, Object e);

	void close();

	void open();
}
