package com.github.thorbenkuck.netcom2.network.shared;

import java.util.function.Consumer;

public interface Pipeline<T> {

	PipelineCondition<T> addLast(Consumer<T> consumer);

	PipelineCondition<T> addFirst(Consumer<T> consumer);

	boolean remove(Consumer<T> pipelineService);

	boolean clear();

	void run(T t);

	void close();

	void open();

	void seal();

	boolean isSealed();

	boolean isOpen();

	void ifClosed(Consumer<Pipeline<T>> consumer);

	void ifClosed(Runnable runnable);

}
