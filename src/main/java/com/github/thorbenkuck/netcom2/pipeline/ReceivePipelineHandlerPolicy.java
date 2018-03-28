package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.exceptions.PipelineAccessException;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This enumeration encapsulates methods to check capabilities of ReceivePipeline handlers in the respective modes.
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
public enum ReceivePipelineHandlerPolicy {

	NOT_ALLOWED {
		/**
		 * {@inheritDoc}
		 */
		@Override
		void prepare(final ReceivePipeline receivePipeline) {
			throw new PipelineAccessException("ReceivePipeline is not allowed to have Object-Handlers");
		}
	}, ALLOW_MULTIPLE {
		/**
		 * {@inheritDoc}
		 */
		@Override
		void prepare(final ReceivePipeline receivePipeline) {
			requireNotSealed(receivePipeline);
		}
	}, ALLOW_SINGLE {
		/**
		 * {@inheritDoc}
		 */
		@Override
		void prepare(final ReceivePipeline receivePipeline) {
			NetCom2Utils.parameterNotNull(receivePipeline);
			requireNotSealed(receivePipeline);
			if (!receivePipeline.isEmpty()) {
				warn("Clearing Pipeline to suit ReceivePipelineHandlerPolicy");
				receivePipeline.clear();
			}
		}

		/**
		 * {@inheritDoc}
		 */
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

	/**
	 * is called, before anything is added to the ReceivePipeline
	 *
	 * @param receivePipeline the pipeline, that something should be added to
	 */
	abstract void prepare(final ReceivePipeline receivePipeline);

	/**
	 * is called, after anything is added to the ReceivePipeline
	 *
	 * @param receivePipeline the pipeline, that something was added to
	 */
	void afterAdding(final ReceivePipeline receivePipeline) {
	}

	/**
	 * Throws an PipelineAccessException if:
	 * <p>
	 * <ul>
	 * <il>the provided pipeline is null</il>
	 * <il>the provided pipeline is sealed</il>
	 * </ul>
	 *
	 * @param receivePipeline the pipeline, that must meet the requirements
	 */
	@APILevel
	final void requireNotSealed(final ReceivePipeline receivePipeline) {
		if (receivePipeline == null) {
			throw new PipelineAccessException("ReceivePipeline is null!");
		}
		if (receivePipeline.isSealed()) {
			fatal("ReceivePipelineHandlerPolicy not applicable to sealed Pipeline!");
			throw new PipelineAccessException("ReceivePipelineHandlerPolicy not applicable to sealed Pipeline!");
		}
	}

	/**
	 * Logs a fatal output to the internal logging.
	 *
	 * @param s the String to log
	 */
	@APILevel
	final void fatal(final String s) {
		logging.fatal(s);
	}

	/**
	 * Logs a trace output to the internal logging.
	 *
	 * @param s the String to log
	 */
	@APILevel
	final void trace(final String s) {
		logging.trace(s);
	}

	/**
	 * Logs a debug output to the internal logging.
	 *
	 * @param s the String to log
	 */
	@APILevel
	final void debug(final String s) {
		logging.debug(s);
	}

	/**
	 * Logs a warn output to the internal logging.
	 *
	 * @param s the String to log
	 */
	@APILevel
	final void warn(final String s) {
		logging.warn(s);
	}
}
