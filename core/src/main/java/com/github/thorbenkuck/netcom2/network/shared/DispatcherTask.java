package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;

import java.util.Collection;

public final class DispatcherTask<T> implements Runnable {

	private static final Logging logging = Logging.unified();
	private final Collection<ReceivePipeline<T>> pipelines;
	private final T t;
	private final ConnectionContext connectionContext;
	private final Session session;

	public DispatcherTask(Collection<ReceivePipeline<T>> pipelines, T t, ConnectionContext connectionContext, Session session) {
		this.pipelines = pipelines;
		this.t = t;
		this.connectionContext = connectionContext;
		this.session = session;
	}

	@Override
	public void run() {
		pipelines.forEach(pipeline -> {
			try {
				logging.trace("Acquiring the pipeline ..");
				pipeline.acquire();
				logging.trace("Running the elements through the Pipeline(#elements=" + pipeline.size() + ".");
				pipeline.run(connectionContext, session, t);
				logging.debug("Successfully applied the ReceivePipeline");
			} catch (final InterruptedException e) {
				logging.catching(e);
			} finally {
				pipeline.release();
				logging.trace("Released the pipeline ..");
			}
		});
	}

	@Override
	public String toString() {
		return "DispatcherTask{" +
				"type=" + t.getClass() +
				'}';
	}
}
