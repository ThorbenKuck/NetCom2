package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.netcom2.logging.Logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class NativeConnectionHandler implements ConnectionHandler {

	private final ConnectionCache connectionCache = ConnectionCache.create();
	private final Logging logging = Logging.unified();

	@Override
	public void prepare(byte[] read) {
		logging.trace("Storing " + read.length);
		connectionCache.append(read);
	}

	private List<String> processNaive(String value) {
		logging.debug("Assuming complete Data-Sets");
		return Arrays.asList(value.split("\\r\\n"));
	}

	private List<String> processParanoid(String value) {
		logging.debug("Assuming incomplete Data-Sets");
		if (!value.contains("\r\n")) {
			logging.debug("Received value has no \\r\\n flag");
			logging.trace("Passing received Value back to the ConnectionCache");
			connectionCache.append(value.getBytes());
			return new ArrayList<>();
		}
		logging.trace("Splitting received value ..");
		String[] subValues = value.split("[\\r\\n]+");
		logging.trace("Instantiating new ResultList");
		final List<String> result = new ArrayList<>();

		logging.trace("checking result data ..");
		if (subValues.length > 1) {
			logging.trace("Found at least on complete Data-Set");
			String[] naiveValues = Arrays.copyOf(subValues, subValues.length - 1);
			logging.trace("Adding complete Data-Sets to the ResultList");
			result.addAll(Arrays.asList(naiveValues));
		}

		logging.trace("Fetching left over byte-data");
		byte[] toCache = subValues[subValues.length - 1].getBytes();
		logging.trace("Passing bytes back to the ConnectionCache");
		connectionCache.append(toCache);

		return result;
	}

	private List<String> processStored(byte[] bytes) {
		logging.trace("Processing read data ..");
		String value = new String(bytes);
		logging.debug("Trying to process " + bytes.length + " bytes");
		logging.trace(value);

		if (value.endsWith("\r\n")) {
			return processNaive(value);
		} else {
			return processParanoid(value);
		}
	}

	@Override
	public List<String> takeNextContents() {
		return processStored(connectionCache.take());
	}
}
