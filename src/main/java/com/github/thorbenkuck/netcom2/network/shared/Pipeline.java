package com.github.thorbenkuck.netcom2.network.shared;

import java.util.function.Consumer;

public interface Pipeline<T> {

	PipelineCondition<T> addLast(final Consumer<T> consumer);

	PipelineCondition<T> addFirst(final Consumer<T> consumer);

	boolean remove(final Consumer<T> pipelineService);

	boolean clear();

	void run(T t);

	void close();

	void open();

	void seal();

	boolean isSealed();

	boolean isOpen();

	void ifClosed(final Consumer<Pipeline<T>> consumer);

	void ifClosed(final Runnable runnable);

}
