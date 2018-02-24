package com.github.thorbenkuck.netcom2.utility;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class AsynchronousIterator<T> implements Iterator<T> {

	private final Queue<T> clients;
	private T currentElement;
	private final Collection<T> source;
	private final boolean removeAllowed;

	@APILevel
	AsynchronousIterator(final Collection<T> collection) {
		this(collection, false);
	}

	@APILevel
	AsynchronousIterator(final Collection<T> collection, boolean removeAllowed) {
		clients = new LinkedList<>(collection);
		this.source = collection;
		this.removeAllowed = removeAllowed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return clients.peek() != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T next() {
		currentElement = clients.poll();
		return currentElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
		if(removeAllowed) {
			source.remove(currentElement);
		} else {
			throw new UnsupportedOperationException("remove");
		}
	}
}