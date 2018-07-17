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

	private void clearByteBuffer(ByteBuffer byteBuffer) {
		byteBuffer.clear();
		byteBuffer.put(new byte[byteBuffer.capacity()]);
		byteBuffer.clear();
	}

	private synchronized void checkOverflow() {
		logging.debug("Checking amount of cached ByteBuffers ..");
		synchronized (priorityQueue) {
			while (priorityQueue.size() > priorityQueueLength.get()) {
				logging.debug("Detected capacity overflow ..");
				logging.trace("Clearing last ByteBuffer of PriorityQueue ..");
				ByteBuffer byteBuffer = priorityQueue.removeLast();
				logging.trace("Removing ByteBuffer fom cache ..");
				synchronized (cache) {
					cache.remove(byteBuffer.capacity());
				}
				logging.trace("Continue check ..");
			}
		}

		logging.trace("Overflow state is okay.");
	}

	private void insertIntoPriorityQueue(ByteBuffer byteBuffer) {
		logging.debug("Inserting ByteBuffer into the PriorityQueue");
		logging.trace("Trying to remove ByteBuffer from PriorityQueue to prevent duplicates ..");
		synchronized (priorityQueue) {
			if (!priorityQueue.remove(byteBuffer)) {
				logging.trace("Could not locate ByteBuffer in PriorityQueue");
			} else {
				logging.trace("ByteBuffer removed from Queue");
			}
			logging.trace("Adding provided ByteBuffer to head of PriorityQueue");
			priorityQueue.addFirst(byteBuffer);
		}
		logging.trace("Requesting capacity check ..");
		checkOverflow();
	}

	private void removeFromPriorityQueue(ByteBuffer byteBuffer) {
		logging.debug("Inserting ByteBuffer into the PriorityQueue");
		logging.trace("Trying to remove ByteBuffer from PriorityQueue to prevent duplicates ..");
		synchronized (priorityQueue) {
			if (!priorityQueue.remove(byteBuffer)) {
				logging.trace("Could not locate ByteBuffer in PriorityQueue");
			} else {
				logging.trace("ByteBuffer removed from Queue");
			}
		}
	}

	private ByteBuffer getOrCreate(int amount) {
		logging.debug("Fetching byteBuffer");
		ByteBuffer byteBuffer;
		logging.trace("Checking empty");
		checkEmpty();
		logging.trace("Fetching ByteBuffer");
		synchronized (priorityQueue) {
			synchronized (cache) {
				byteBuffer = cache.remove(amount);
			}
			if (byteBuffer == null) {
				logging.debug("No matching ByteBuffer found. Allocating new ByteBuffer");
				return create(amount);
			}
			logging.trace("Requesting add of ByteBuffer");
			removeFromPriorityQueue(byteBuffer);
			logging.trace("Clearing ByteBuffer, just to be sure");
		}
		clearByteBuffer(byteBuffer);

		return byteBuffer;
	}

	private void checkEmpty() {
		logging.debug("Checking if ByteBuffer is needed");
		synchronized (priorityQueue) {
			synchronized (cache) {
				logging.trace("Acquired PriorityQueue");
				if (priorityQueue.isEmpty()) {
					logging.trace("PriorityQueue is empty");
					logging.debug("Requesting new ByteBuffer");
					ByteBuffer buffer = create(1024);
					logging.trace("Adding ByteBuffer to Cache");
					cache.put(buffer.capacity(), buffer);
					logging.trace("Adding ByteBuffer to PriorityQueue");
					insertIntoPriorityQueue(buffer);
				}
			}
		}
	}

	ByteBuffer allocate(byte[] data) {
		ByteBuffer buffer = allocate(data.length);
		buffer.put(data);
		buffer.flip();

		return buffer;
	}

	ByteBuffer allocate(int amount) {
		return getOrCreate(amount);
	}

	ByteBuffer allocate() {
		ByteBuffer buffer;
		synchronized (priorityQueue) {
			checkEmpty();
			logging.trace("Fetching first ByteBuffer and clearing it from PriorityQueue");
			buffer = priorityQueue.removeFirst();
			if (buffer == null) {
				logging.debug("Received a faulty ByteBuffer. Trying again");
				checkEmpty();
				logging.trace("Fetching ..");
				buffer = priorityQueue.removeFirst();
			}
			logging.trace("Clearing from cache..");

			synchronized (cache) {
				cache.remove(buffer.capacity());
				logging.debug("PriorityQueue=" + priorityQueue);
				logging.debug("Cache=" + cache);
			}
		}
		clearByteBuffer(buffer);

		return buffer;
	}

	void free(ByteBuffer byteBuffer) {
		logging.debug("Freeing given ByteBuffer");
		synchronized (priorityQueue) {
			logging.trace("Acquired priorityQueue");
			synchronized (cache) {
				logging.trace("Acquired Cache");
				logging.trace("Adding ByteBuffer to PriorityQueue");
				priorityQueue.addFirst(byteBuffer);
				logging.trace("Adding ByteBuffer to Cache");
				cache.put(byteBuffer.capacity(), byteBuffer);
			}
		}
		logging.trace("Requesting overflow check");
		checkOverflow();
	}
}
