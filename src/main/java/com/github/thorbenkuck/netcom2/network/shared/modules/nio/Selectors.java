package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import java.io.IOException;
import java.nio.channels.Selector;

class Selectors {

	private final Selector selector;
	private final Selector receiver;

	Selectors() throws IOException {
		this.selector = Selector.open();
		this.receiver = Selector.open();
	}

	public Selector getSelector() {
		return selector;
	}

	public Selector getReceiver() {
		return receiver;
	}
}
