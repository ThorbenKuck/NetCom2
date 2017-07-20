package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;

public class Wrapper {

	public <T> OnReceiveTriple<T> wrap(OnReceive<T> onReceive) {
		return new OnReceiveWrapper<>(onReceive);
	}

	public <T> OnReceiveTriple<T> wrap(OnReceiveSingle<T> onReceive) {
		return new OnReceiveSingleWrapper<>(onReceive);
	}

}
