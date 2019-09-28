package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.shared.OnReceive;
import com.github.thorbenkuck.netcom2.shared.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.shared.OnReceiveTriple;

/**
 * <p>This interface describes, that an implementation can be registered to anything.</p>
 * <p>
 * If the using Module knows this interface, it calls <code>onRegistration</code>, when the implementing class is registered
 * and <code>onUnRegistration</code>, once the implementing class is unregistered
 * </p>
 * <p>
 * Known Implementations are:
 * <ul>
 * <li>{@link OnReceiveSingle}</li>
 * <li>{@link OnReceive}</li>
 * <li>{@link OnReceiveTriple}</li>
 * </ul>
 *
 * @version 1.0
 * @since 1.0
 */
public interface CanBeRegistered {

	/**
	 * This method is called, when the implementing Class is registered. It can be changed, but it is not necessary to
	 */
	default void onUnRegistration() {
	}

	/**
	 * This method is called, when the implementing Class is unregistered. It can be changed, but it is not necessary to
	 */
	default void onRegistration() {
	}

	/**
	 * This method is called, if for example the ReceivePipeline is closed, but an instance of CanBeRegistered is added.
	 * First {@link #onUnRegistration()} is called, than this, lastly {@link #onUnRegistration()}. This way, an
	 */
	default void onAddFailed() {
	}
}
