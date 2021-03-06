package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.keller.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.shared.OnReceive;
import com.github.thorbenkuck.netcom2.shared.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.shared.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.utils.NetCom2Utils;

/**
 * This Class is used to encapsulate the {@link OnReceiveWrapper} class, which is not Public
 * <p>
 * If you want to wrap an {@link OnReceive} or {@link OnReceiveSingle} to be encapsulated within an {@link OnReceiveTriple},
 * which is used within the {@link com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline}, you may use this class.
 * <p>
 * For most developers, this will not be necessary.
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
public class Wrapper {

	/**
	 * Wraps an {@link OnReceive} to be encapsulated by an {@link OnReceiveTriple}.
	 *
	 * @param onReceive the OnReceive, that should be encapsulated
	 * @param <T>       the generic type of the {@link OnReceive}
	 * @return an {@link OnReceiveTriple}
	 */
	public <T> OnReceiveTriple<T> wrap(final OnReceive<T> onReceive) {
		NetCom2Utils.parameterNotNull(onReceive);
		return new OnReceiveWrapper<>(onReceive);
	}

	/**
	 * Wraps an {@link OnReceiveSingle} to be encapsulated by an {@link OnReceiveTriple}.
	 *
	 * @param onReceive the OnReceiveSingle, that should be encapsulated
	 * @param <T>       the generic type of the {@link OnReceiveSingle}
	 * @return an {@link OnReceiveTriple}
	 */
	public <T> OnReceiveTriple<T> wrap(final OnReceiveSingle<T> onReceive) {
		NetCom2Utils.parameterNotNull(onReceive);
		return new OnReceiveSingleWrapper<>(onReceive);
	}

}
