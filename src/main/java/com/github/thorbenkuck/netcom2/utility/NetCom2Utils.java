package com.github.thorbenkuck.netcom2.utility;

import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.pipeline.Wrapper;

import java.util.Objects;

public class NetCom2Utils {
	public static void assertNotNull(final Object o) {
		Objects.requireNonNull(o);
	}

	public static void assertNotNull(final Object... objects) {
		for (final Object object : objects) {
			assertNotNull(object);
		}
	}

	public static void parameterNotNull(final Object object) {
		if (object == null) {
			throw new IllegalArgumentException("Null is not a valid parameter!");
		}
	}

	public static void parameterNotNull(final Object... objects) {
		for (final Object object : objects) {
			parameterNotNull(object);
		}
	}

	public static <T> OnReceiveTriple<T> wrap(OnReceiveSingle<T> onReceiveSingle) {
		return new Wrapper().wrap(onReceiveSingle);
	}

	public static <T> OnReceiveTriple<T> wrap(OnReceive<T> onReceive) {
		return new Wrapper().wrap(onReceive);
	}
}
