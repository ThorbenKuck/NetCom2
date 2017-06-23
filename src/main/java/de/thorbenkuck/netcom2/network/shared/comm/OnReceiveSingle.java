package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.pipeline.CanBeRegistered;

import java.util.function.Consumer;

public interface OnReceiveSingle<O> extends Consumer<O>, CanBeRegistered {

	void accept(O o);

}
