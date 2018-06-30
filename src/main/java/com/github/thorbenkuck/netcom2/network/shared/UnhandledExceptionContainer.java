package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.netcom2.logging.Logging;

import java.util.function.Consumer;

public class UnhandledExceptionContainer {

	private static final Pipeline<Throwable> pipeline = Pipeline.unifiedCreation();

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
			pipeline.apply(e);
		}
	}

}
