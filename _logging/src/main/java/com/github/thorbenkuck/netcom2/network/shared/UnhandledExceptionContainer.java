package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.logging.Logging;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UnhandledExceptionContainer {

	private static final List<Consumer<Throwable>> pipeline = new ArrayList<>();

	static {
		Logging logging = Logging.unified();
		pipeline.add(logging::catching);
	}

	public static void addHandler(Consumer<Throwable> exceptionConsumer) {
		synchronized (pipeline) {
			pipeline.add(exceptionConsumer);
		}
	}

	public static void catching(Throwable e) {
		synchronized (pipeline) {
			pipeline.forEach(c -> c.accept(e));
		}
	}

}
