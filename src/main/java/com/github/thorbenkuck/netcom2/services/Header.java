package com.github.thorbenkuck.netcom2.services;

import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Header implements Iterable<Header.Entry> {

	private final List<Entry> entries = new ArrayList<>();
	private final List<String> takenKeys = new ArrayList<>();
	private String keyValueSeparator = ":";
	private String entrySeparator = ",";

	public Header() {
	}

	public Header(String rawHeader) {
		String[] newEntries = rawHeader.split(entrySeparator);
		for (String string : newEntries) {
			addEntry(new Entry(string));
		}
	}

	public Header(Header header) {
		header.forEach(this::addEntry);
	}

	private String construct() {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < entries.size() - 1; i++) {
			stringBuilder.append(entries.get(i)).append(",");
		}
		stringBuilder.append(entries.get(entries.size() - 1));

		return stringBuilder.toString();
	}

	public void addEntry(Entry entry) {
		entries.add(entry);
		takenKeys.add(entry.key());
	}

	public final void addEntry(String key, String value) {
		if (!takenKeys.contains(key)) {
			addEntry(new Entry(key, value));
		}
	}

	public String get(String key) {
		for (Entry entry : this) {
			if (entry.key.equals(key)) {
				return entry.value;
			}
		}
		return "KEY(" + key + ") NOT_FOUND";
	}

	@Override
	public final String toString() {
		return construct();
	}

	@Override
	public Iterator<Entry> iterator() {
		return NetCom2Utils.createAsynchronousIterator(entries);
	}

	public class Entry {
		private final String key;
		private final String value;

		public Entry(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public Entry(String keyValue) {
			if (!keyValue.contains(keyValueSeparator)) {
				throw new IllegalArgumentException("Incorrect format " + keyValue);
			}
			String[] split = keyValue.split(keyValueSeparator);
			if (split.length != 2) {
				throw new IllegalArgumentException("Incorrect format " + keyValue);
			}
			this.key = split[0];
			this.value = split[1];
		}

		public String key() {
			return key;
		}

		public String value() {
			return value;
		}

		@Override
		public String toString() {
			return key + ":" + value;
		}
	}
}
