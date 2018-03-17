package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.exceptions.PipelineAccessException;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

public enum ReceivePipelineHandlerPolicy {

	NOT_ALLOWED {
		@Override
		void prepare(final ReceivePipeline receivePipeline) {
			throw new PipelineAccessException("ReceivePipeline is not allowed to have Object-Handlers");
		}
	}, ALLOW_MULTIPLE {
		@Override
		void prepare(final ReceivePipeline receivePipeline) {
			requireNotSealed(receivePipeline);
		}
	}, ALLOW_SINGLE {
		@Override
		void prepare(final ReceivePipeline receivePipeline) {
			NetCom2Utils.parameterNotNull(receivePipeline);
			requireNotSealed(receivePipeline);
			if (! receivePipeline.isEmpty()) {
				warn("Clearing Pipeline to suit ReceivePipelineHandlerPolicy");
				receivePipeline.clear();
			}
		}

		@Override
		void afterAdding(final ReceivePipeline receivePipeline) {
			NetCom2Utils.parameterNotNull(receivePipeline);
			trace("Closing pipeline " + receivePipeline + " ..");
			receivePipeline.close();
			trace("Sealing closed Pipeline ..");
			receivePipeline.seal();
			debug("Blocked all access and sealed the pipeline " + receivePipeline + "!");
			debug("The Pipeline will only consist of the provided Object-Handler, except if recreated and redeployed!");
		}
	};

	private final Logging logging = Logging.unified();

	abstract void prepare(final ReceivePipeline receivePipeline);

	void afterAdding(final ReceivePipeline receivePipeline) {
	}

	@APILevel
	final void requireNotSealed(final ReceivePipeline receivePipeline) {
		if(receivePipeline == null) {
			throw new PipelineAccessException("ReceivePipeline is null!");
		}
		if (receivePipeline.isSealed()) {
			fatal("ReceivePipelineHandlerPolicy not applicable to sealed Pipeline!");
			throw new PipelineAccessException("ReceivePipelineHandlerPolicy not applicable to sealed Pipeline!");
		}
	}

	@APILevel
	final void fatal(final String s) {
		logging.fatal(s);
	}

	@APILevel
	final void trace(final String s) {
		logging.trace(s);
	}

	@APILevel
	final void debug(final String s) {
		logging.debug(s);
	}

	@APILevel
	final void warn(final String s) {
		logging.warn(s);
	}
}
