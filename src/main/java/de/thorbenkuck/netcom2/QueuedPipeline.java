package de.thorbenkuck.netcom2;

import de.thorbenkuck.netcom2.interfaces.Pipeline;
import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;

import java.util.LinkedList;
import java.util.Queue;

public class QueuedPipeline<T> implements Pipeline<T> {

	private Queue<OnReceive<T>> core = new LinkedList<>();

	@Override
	public void addLast(OnReceive<T> pipelineService) {
		core.add(pipelineService);
	}

	@Override
	public void addFirst(OnReceive<T> onReceive) {
		Queue<OnReceive<T>> newCore = new LinkedList<>();
		newCore.add(onReceive);
		newCore.addAll(core);
		core.clear();
		core.addAll(newCore);
	}

	@Override
	public void remove(OnReceive<T> pipelineService) {
		core.remove(pipelineService);
	}

	@Override
	public void clear() {
		core.clear();
	}

	@Override
	public void run(User user, Object t) {
		core.forEach(onReceive -> onReceive.run(user, (T) t));
	}
}
