package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

class NIOBuffer {

	private final Map<Integer, ByteBuffer> cache = new HashMap<>();
	private final Deque<ByteBuffer> priorityQueue = new ArrayDeque<>();
	private final Logging logging = Logging.unified();
	private final Value<Integer> priorityQueueLength = Value.synchronize(10);

	NIOBuffer() {
		logging.instantiated(this);
	}

	private ByteBuffer create(int amount) {
		logging.debug("Creating new ByteBuffer with a capacity of " + amount);
		return ByteBuffer.allocate(amount);
	}

	private synchronized void checkOverflow() {
		logging.debug("Checking amount of cached ByteBuffers ..");
		while (priorityQueue.size() > priorityQueueLength.get()) {
			logging.debug("Detected capacity overflow ..");
			logging.trace("Clearing last ByteBuffer of PriorityQueue ..");
			ByteBuffer byteBuffer = priorityQueue.removeLast();
			logging.trace("Removing ByteBuffer fom cache ..");
			cache.remove(byteBuffer.capacity());
			logging.trace("Continue check ..");
		}

		logging.trace("Buffer is okay.");
	}

	private void insertIntoPriorityQueue(ByteBuffer byteBuffer) {
		logging.debug("Inserting ByteBuffer into the PriorityQueue");
		logging.trace("Trying to remove ByteBuffer from PriorityQueue to prevent duplicates ..");
		if (!priorityQueue.remove(byteBuffer)) {
			logging.trace("Could not locate ByteBuffer in PriorityQueue");
		} else {
			logging.trace("ByteBuffer removed from Queue");
		}
		logging.trace("Adding provided ByteBuffer to head of PriorityQueue");
		priorityQueue.addFirst(byteBuffer);
		logging.trace("Requesting capacity check ..");
		checkOverflow();
	}

	private ByteBuffer getOrCreate(int amount) {
		logging.debug("Fetching byteBuffer");
		logging.trace("Trying to create not cached ByteBuffer");
		ByteBuffer byteBuffer;
		synchronized (cache) {
			cache.computeIfAbsent(amount, this::create);
			logging.trace("Fetching ByteBuffer");
			byteBuffer = cache.get(amount);
		}
		logging.trace("Requesting add of ByteBuffer");
		insertIntoPriorityQueue(byteBuffer);
		logging.trace("Clearing ByteBuffer, just to be sure");
		byteBuffer.clear();

		return byteBuffer;
	}

	private synchronized void checkEmpty() {
		logging.debug("Checking if ByteBuffer is needed");
		synchronized (priorityQueue) {
			logging.trace("Acquired PriorityQueue");
			if (priorityQueue.isEmpty()) {
				logging.trace("PriorityQueue is empty. Creating new ByteBuffer with 1024 capacity");
				priorityQueue.addFirst(create(1024));
			}
		}
	}

	ByteBuffer allocate(byte[] data) {
		return ByteBuffer.wrap(data);
	}

	ByteBuffer allocate(int amount) {
		return ByteBuffer.allocate(amount);

	}

	void free(ByteBuffer byteBuffer) {
		logging.debug("Freeing given ByteBuffer");
		synchronized (priorityQueue) {
			synchronized (cache) {
				priorityQueue.addFirst(byteBuffer);
				cache.put(byteBuffer.capacity(), byteBuffer);
			}
		}
		checkOverflow();
	}

	ByteBuffer allocate() {
		// return ByteBuffer.allocate(1024);
		ByteBuffer buffer;
		checkEmpty();
		synchronized (priorityQueue) {
			synchronized (cache) {
				buffer = priorityQueue.removeFirst();
				cache.remove(buffer.capacity());
			}
		}
		buffer.clear();

		return buffer;
	}
}
