package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.exceptions.PipelineAccessException;
import de.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import de.thorbenkuck.netcom2.network.interfaces.Logging;

public enum ReceivePipelineHandlerPolicy {

	NOT_ALLOWED {
		@Override
		void prepare(ReceivePipeline receivePipeline) {
			throw new PipelineAccessException("ReceivePipeline is not allowed to have Object-Handlers");
		}
	}, ALLOW_MULTIPLE {
		@Override
		void prepare(ReceivePipeline receivePipeline) {
			requireNotSealed(receivePipeline);
		}
	}, ALLOW_SINGLE {
		@Override
		void prepare(ReceivePipeline receivePipeline) {
			requireNotSealed(receivePipeline);
			if (! receivePipeline.isEmpty()) {
				warn("Clearing Pipeline to suit ReceivePipelineHandlerPolicy");
				receivePipeline.clear();
			}
		}

		@Override
		void afterAdding(ReceivePipeline receivePipeline) {
			trace("Closing pipeline " + receivePipeline + " ..");
			receivePipeline.close();
			trace("Sealing closed Pipeline ..");
			receivePipeline.seal();
			debug("Blocked all access and sealed the pipeline " + receivePipeline + "!");
			debug("The Pipeline will only consist of the provided Object-Handler, except if cleared and redeployed!");
		}
	};

	private final Logging logging = Logging.unified();

	abstract void prepare(ReceivePipeline receivePipeline);

	void afterAdding(ReceivePipeline receivePipeline) {
	}

	final void requireNotSealed(ReceivePipeline receivePipeline) {
		if (receivePipeline.isSealed()) {
			fatal("ReceivePipelineHandlerPolicy not applicable to sealed Pipeline!");
			throw new PipelineAccessException("ReceivePipelineHandlerPolicy not applicable to sealed Pipeline!");
		}
	}

	final void fatal(String s) {
		logging.fatal(s);
	}

	final void trace(String s) {
		logging.trace(s);
	}

	final void debug(String s) {
		logging.debug(s);
	}

	final void warn(String s) {
		logging.warn(s);
	}
}
